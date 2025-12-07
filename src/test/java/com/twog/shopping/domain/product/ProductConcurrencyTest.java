package com.twog.shopping.domain.product;

import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import com.twog.shopping.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException; // Import for specific exception

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductConcurrencyTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품 재고 동시성 테스트: 동시에 2명이 주문 시 낙관적 락 발생 확인")
    void concurrencyTest() throws InterruptedException {
        // 1. 상품 생성 (재고 1개)
        Product product = Product.builder()
                .productName("한정판 스니커즈")
                .productCategory("신발")
                .productPrice(100000)
                .productQuantity(1) // 딱 1개 남음
                .productStatus(ProductStatus.ACTIVE)
                .build();
        Product savedProduct = productRepository.save(product);

        // 2. 동시성 테스트 준비
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 3. 동시에 재고 감소 시도
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // 트랜잭션 단위로 실행되어야 함 (실제 서비스에서는 @Transactional 메소드 호출)
                    // 여기서는 테스트를 위해 직접 Repo에서 조회 후 수정 후 저장
                    decreaseStockWithLock(savedProduct.getProductId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("구매 실패: " + e.getClass().getName()); // 예외 로그 출력
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 두 스레드가 끝날 때까지 대기

        // 4. 검증
        // 1명만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        // 1명은 실패해야 함 (낙관적 락 예외)
        assertThat(failCount.get()).isEqualTo(1);

        // 최종 재고는 0이어야 함
        Product finalProduct = productRepository.findById(savedProduct.getProductId()).orElseThrow();
        assertThat(finalProduct.getProductQuantity()).isEqualTo(0);
    }

    // 서비스 메소드를 흉내낸 헬퍼 메소드 (트랜잭션 분리를 위해 별도 클래스로 빼거나 여기서 로직 처리)
    // 주의: @Transactional이 없으면 영속성 컨텍스트가 유지되지 않아 save 호출 시 버전 체크가 일어남
    public void decreaseStockWithLock(int productId) {
        // 1. 조회 (Version 0)
        Product product = productRepository.findById(productId).orElseThrow();

        // 2. 재고 감소
        product.decreaseStock(1);

        // 3. 저장 (Version 0 -> 1 시도)
        // 만약 다른 스레드가 먼저 1로 올렸다면, 여기서 예외 발생
        productRepository.save(product);
    }
}
