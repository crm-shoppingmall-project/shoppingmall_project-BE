package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy; // Spy 추가
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @InjectMocks
    private PurchaseService purchaseService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    private Member testMember;
    private Purchase testPurchase;

    private final Long TEST_MEMBER_ID = 1L;
    private final int TEST_PRODUCT_ID = 1;
    private final Long TEST_PURCHASE_ID = 1L;
    private final int PRODUCT_PRICE = 10000;

    // @Spy를 사용하여 실제 객체를 생성하되, 일부 메서드의 동작을 변경할 수 있도록 함
    @Spy
    private Product testProduct = Product.builder()
            .productPrice(PRODUCT_PRICE)
            .productQuantity(100)
            .build();

    @BeforeEach
    void setUp() {
        // testProduct.getProductId()가 항상 TEST_PRODUCT_ID를 반환하도록 설정
        doReturn(TEST_PRODUCT_ID).when(testProduct).getProductId();

        testMember = Member.builder()
                .memberId(TEST_MEMBER_ID)
                .build();

        testPurchase = Purchase.builder()
                .id(TEST_PURCHASE_ID)
                .memberId(TEST_MEMBER_ID)
                .status(PurchaseStatus.REQUESTED)
                .build();

        PurchaseDetail detail = PurchaseDetail.builder()
                .productId(testProduct.getProductId())
                .quantity(2)
                .paidAmount(PRODUCT_PRICE)
                .build();
        testPurchase.addDetail(detail);
    }

//    @Test
//    @DisplayName("주문 생성 성공")
//    void createPurchase_Success() {
//        // given
//        int orderQuantity = 2;
//        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto(TEST_PRODUCT_ID, orderQuantity, PRODUCT_PRICE);
//        PurchaseRequest request = new PurchaseRequest(PRODUCT_PRICE * orderQuantity, Collections.singletonList(itemDto));
//
//        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(testProduct));
//        when(purchaseRepository.save(any(Purchase.class))).thenReturn(testPurchase);
//
//        // when
//        Long purchaseId = purchaseService.createPurchase(request, TEST_MEMBER_ID);
//
//        // then
//        assertThat(purchaseId).isEqualTo(TEST_PURCHASE_ID);
//        verify(productRepository, times(1)).findById(TEST_PRODUCT_ID);
//        verify(purchaseRepository, times(1)).save(any(Purchase.class));
//    }

    @Test
    @DisplayName("내 주문 목록 조회 성공")
    void findMyPurchases_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Purchase> purchases = Collections.singletonList(testPurchase);
        Page<Purchase> purchasePage = new PageImpl<>(purchases, pageable, purchases.size());

        when(purchaseRepository.findByMemberId(TEST_MEMBER_ID, pageable)).thenReturn(purchasePage);

        // when
        Page<PurchaseResponse> result = purchaseService.findMyPurchases(TEST_MEMBER_ID, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPurchaseId()).isEqualTo(TEST_PURCHASE_ID);
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelPurchase_Success() {
        // given
        when(purchaseRepository.findById(TEST_PURCHASE_ID)).thenReturn(Optional.of(testPurchase));
        when(productRepository.findById(anyInt())).thenReturn(Optional.of(testProduct));

        // when
        purchaseService.cancelPurchase(TEST_PURCHASE_ID, TEST_MEMBER_ID);

        // then
        verify(purchaseRepository, times(1)).findById(TEST_PURCHASE_ID);
        verify(productRepository, times(1)).findById(anyInt());
        assertThat(testPurchase.getStatus()).isEqualTo(PurchaseStatus.REJECTED);
    }
}
