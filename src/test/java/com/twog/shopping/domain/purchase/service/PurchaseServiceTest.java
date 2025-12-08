package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseDetailRepository;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.domain.purchase.repository.ReturnRequestRepository;
import com.twog.shopping.global.common.entity.GradeName;
import com.twog.shopping.global.error.exception.OutOfStockException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PurchaseServiceTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseDetailRepository purchaseDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberGradeRepository memberGradeRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private EntityManager entityManager;

    private Member member;
    private Product product;

    private final int INITIAL_PRICE = 10000;
    private final int INITIAL_QUANTITY = 100;

    @BeforeEach
    @Transactional
    void setUp() {
        // 데이터 클리어 (외래 키 제약 조건에 따른 순서)
        paymentRepository.deleteAllInBatch();
        returnRequestRepository.deleteAllInBatch();
        purchaseDetailRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberGradeRepository.deleteAllInBatch();

        MemberGrade memberGrade = memberGradeRepository.save(MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build());

        member = memberRepository.saveAndFlush(Member.builder()
                .memberName("테스트유저")
                .memberEmail("testuser@example.com")
                .memberPwd("hashed_password")
                .memberGrade(memberGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .build());

        product = productRepository.saveAndFlush(Product.builder()
                .productName("테스트 상품")
                .productCategory("테스트 카테고리")
                .productPrice(INITIAL_PRICE)
                .productQuantity(INITIAL_QUANTITY)
                .build());
    }

    @AfterEach
    @Transactional
    void tearDown() {
        // 데이터 클리어 (외래 키 제약 조건에 따른 순서)
        paymentRepository.deleteAllInBatch();
        returnRequestRepository.deleteAllInBatch();
        purchaseDetailRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberGradeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("주문 생성 통합 테스트 - 성공")
    @Transactional
    void createPurchase_Success_Integration() {
        // given
        int orderQuantity = 5;
        Integer expectedTotalPrice = INITIAL_PRICE * orderQuantity;

        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto(
                (long) product.getProductId(), // Product.productId가 int이므로 Long으로 캐스팅
                orderQuantity,
                INITIAL_PRICE
        );
        PurchaseRequest request = new PurchaseRequest(expectedTotalPrice, Collections.singletonList(itemDto));

        // when
        Long purchaseId = purchaseService.createPurchase(request, member.getMemberId());

        // then
        entityManager.flush();
        entityManager.clear();

        Purchase foundPurchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new AssertionError("저장된 주문을 찾을 수 없습니다."));

        Product updatedProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new AssertionError("상품을 찾을 수 없습니다."));

        assertThat(updatedProduct.getProductQuantity()).isEqualTo(INITIAL_QUANTITY - orderQuantity);
        assertThat(foundPurchase.getDetails().getFirst().getPaidAmount()).isEqualTo(product.getProductPrice());
    }

    @Test
    @DisplayName("주문 생성 통합 테스트 - 실패 (재고 부족)")
    @Transactional
    void createPurchase_Fail_OutOfStock_Integration() {
        // given
        int orderQuantity = INITIAL_QUANTITY + 1;
        int price = INITIAL_PRICE;
        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), orderQuantity, price);
        PurchaseRequest request = new PurchaseRequest(price * orderQuantity, Collections.singletonList(itemDto));

        // when & then
        assertThrows(OutOfStockException.class, () -> purchaseService.createPurchase(request, member.getMemberId()));
    }

    @Test
    @DisplayName("동시에 주문하여 재고 차감 시 낙관적 락 충돌 검증")
    void decreaseStock_Concurrency_OptimisticLock() throws InterruptedException {
        // given
        int numberOfThreads = 10;
        int orderQuantity = 1;
        int price = INITIAL_PRICE;

        AtomicLong successCount = new AtomicLong(0);

        // when
        try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), orderQuantity, price);
                        PurchaseRequest request = new PurchaseRequest(price * orderQuantity, Collections.singletonList(itemDto));

                        purchaseService.createPurchase(request, member.getMemberId());
                        successCount.incrementAndGet();

                    } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                        // 낙관적 락 충돌 발생
                    } catch (Exception e) {
                        // 기타 예외
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        entityManager.flush();
        entityManager.clear();

        // then
        Product updatedProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new AssertionError("상품을 찾을 수 없습니다."));

        long finalSuccessfulPurchases = successCount.get();

        System.out.println("초기 재고: " + INITIAL_QUANTITY);
        System.out.println("성공한 주문 수: " + finalSuccessfulPurchases);
        System.out.println("남은 재고: " + updatedProduct.getProductQuantity());

        assertThat(updatedProduct.getProductQuantity()).isEqualTo(INITIAL_QUANTITY - (int) finalSuccessfulPurchases);
        assertThat(finalSuccessfulPurchases).isLessThan(numberOfThreads);
    }

    @Test
    @DisplayName("주문 생성 통합 테스트 - 실패 (상품 정보 없음)")
    @Transactional
    void createPurchase_Fail_ProductNotFound_Integration() {
        // given
        Long nonExistentProductId = 9999L;
        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto(nonExistentProductId, 1, INITIAL_PRICE);
        PurchaseRequest request = new PurchaseRequest(INITIAL_PRICE, Collections.singletonList(itemDto));

        // when & then
        assertThrows(NoSuchElementException.class, () -> purchaseService.createPurchase(request, member.getMemberId()));
    }

    @Test
    @DisplayName("주문 생성 통합 테스트 - 실패 (총 결제 금액 불일치)")
    @Transactional
    void createPurchase_Fail_TotalAmountMismatch_Integration() {
        // given
        Integer incorrectTotalAmount = 1;
        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), 1, INITIAL_PRICE);
        PurchaseRequest request = new PurchaseRequest(incorrectTotalAmount, Collections.singletonList(itemDto));

        // when & then
        assertThrows(IllegalStateException.class, () -> purchaseService.createPurchase(request, member.getMemberId()));
    }

    @Test
    @DisplayName("내 주문 목록 조회 - 성공")
    @Transactional
    void findMyPurchases_Success() {
        // given
        // 2개의 주문 생성
        purchaseService.createPurchase(new PurchaseRequest(INITIAL_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), 1, INITIAL_PRICE))), member.getMemberId());
        purchaseService.createPurchase(new PurchaseRequest(INITIAL_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), 1, INITIAL_PRICE))), member.getMemberId());

        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<PurchaseResponse> result = purchaseService.findMyPurchases(member.getMemberId(), pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getMemberId()).isEqualTo(member.getMemberId());
        assertThat(result.getContent().get(0).getTotalAmount()).isEqualTo(INITIAL_PRICE);
    }

    @Test
    @DisplayName("주문 취소 - 성공")
    @Transactional
    void cancelPurchase_Success() {
        // given
        Long purchaseId = purchaseService.createPurchase(new PurchaseRequest(INITIAL_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), 1, INITIAL_PRICE))), member.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // when
        purchaseService.cancelPurchase(purchaseId, member.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // then
        Purchase cancelledPurchase = purchaseRepository.findById(purchaseId).orElseThrow();
        Product restockedProduct = productRepository.findById(product.getProductId()).orElseThrow();

        assertThat(cancelledPurchase.getStatus()).isEqualTo(PurchaseStatus.REJECTED);
        assertThat(restockedProduct.getProductQuantity()).isEqualTo(INITIAL_QUANTITY);
    }

    @Test
    @DisplayName("주문 취소 - 실패 (취소 불가능한 상태)")
    @Transactional
    void cancelPurchase_Fail_InvalidStatus() {
        // given
        Long purchaseId = purchaseService.createPurchase(new PurchaseRequest(INITIAL_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) product.getProductId(), 1, INITIAL_PRICE))), member.getMemberId());
        Purchase purchase = purchaseRepository.findById(purchaseId).orElseThrow();
        purchase.updateStatus(PurchaseStatus.COMPLETED);
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThrows(IllegalStateException.class, () -> purchaseService.cancelPurchase(purchaseId, member.getMemberId()));
    }
}