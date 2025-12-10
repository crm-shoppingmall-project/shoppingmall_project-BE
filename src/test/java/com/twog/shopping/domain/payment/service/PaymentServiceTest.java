package com.twog.shopping.domain.payment.service;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.payment.dto.PaymentRequest;
import com.twog.shopping.domain.payment.dto.PaymentResponse;
import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.entity.PaymentType;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.global.config.TossPaymentsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private TossPaymentsConfig tossPaymentsConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Member testMember;
    private Purchase testPurchase;
    private Payment testPayment;
    private PaymentRequest paymentRequest;

    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_PURCHASE_ID = 100L;
    private final Long TEST_PAYMENT_ID = 1L;
    private final Integer TEST_AMOUNT = 10000;
    private final String TEST_PAYMENT_KEY = "test_payment_key_from_toss";
    private final String TEST_ORDER_ID = String.valueOf(TEST_PURCHASE_ID); // orderId는 purchaseId와 동일

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .memberId(TEST_MEMBER_ID)
                .build();

        testPurchase = Purchase.builder()
                .id(TEST_PURCHASE_ID)
                .memberId(TEST_MEMBER_ID)
                .status(PurchaseStatus.REQUESTED)
                .build();

        PurchaseDetail detail = PurchaseDetail.builder()
                .productId(1)
                .quantity(1)
                .paidAmount(TEST_AMOUNT)
                .build();
        testPurchase.addDetail(detail);

        // initiatePayment 후 저장될 Payment 객체 (pgTid는 null)
        testPayment = Payment.builder()
                .id(TEST_PAYMENT_ID)
                .purchase(testPurchase)
                .pgTid(null) // 초기에는 null
                .status(PaymentStatus.REQUESTED)
                .type(PaymentType.PAYMENT) // PaymentType.PAYMENT 사용
                .build();

        // PaymentRequest는 paymentKey 필드가 없음
        paymentRequest = new PaymentRequest(
                TEST_PURCHASE_ID,
                TEST_AMOUNT,
                PaymentType.PAYMENT // PaymentType.PAYMENT 사용
        );
    }

    @Test
    @DisplayName("결제 초기화 성공")
    void initiatePayment_Success() {
        when(purchaseRepository.findById(TEST_PURCHASE_ID)).thenReturn(Optional.of(testPurchase));
        when(paymentRepository.findByPurchase_Id(TEST_PURCHASE_ID)).thenReturn(Optional.empty());

        // initiatePayment에서 save될 Payment 객체는 pgTid가 null이어야 함
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenReturn(testPayment);

        Long paymentId = paymentService.initiatePayment(paymentRequest, TEST_MEMBER_ID);

        assertThat(paymentId).isEqualTo(TEST_PAYMENT_ID);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertThat(paymentCaptor.getValue().getPgTid());
    }

    @Test
    @DisplayName("토스 결제 승인 성공")
    void confirmTossPayment_Success() {
        // Given
        // confirmTossPayment는 orderId (paymentId)로 Payment를 찾음
        // testPayment의 pgTid는 아직 null 상태여야 함 (initiatePayment 직후)
        testPayment.updatePgTid(TEST_PAYMENT_KEY);

        // findByPurchase_Id 대신 findById를 Mocking
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(testPayment));
        when(tossPaymentsConfig.getSecretKey()).thenReturn("test_secret_key");
        when(tossPaymentsConfig.getApiUrl()).thenReturn("https://api.tosspayments.com/v1/payments"); // /confirm 제외

        // RestTemplate 응답 Mocking
        ResponseEntity<Map> mockApiResponse = new ResponseEntity<>(Map.of("status", "DONE"), HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq("https://api.tosspayments.com/v1/payments/confirm"), // 정확한 URL
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(mockApiResponse);

        // orderId를 TEST_PAYMENT_ID와 동일하게 설정
        paymentService.confirmTossPayment(TEST_PAYMENT_KEY, String.valueOf(TEST_PAYMENT_ID), TEST_AMOUNT);

        // Payment와 Purchase 상태 변경 및 pgTid 업데이트 확인
        verify(paymentRepository, times(1)).save(testPayment);
        verify(purchaseRepository, times(1)).save(testPurchase);
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(testPurchase.getStatus()).isEqualTo(PurchaseStatus.COMPLETED);
        assertThat(testPayment.getPgTid()).isEqualTo(TEST_PAYMENT_KEY); // pgTid가 업데이트되었는지 확인
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() {
        // 취소는 COMPLETED 상태의 결제에 대해 이루어져야 함
        testPayment.setStatus(PaymentStatus.COMPLETED);
        testPayment.updatePgTid(TEST_PAYMENT_KEY); // pgTid가 있어야 취소 API 호출 가능
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(testPayment));
        when(tossPaymentsConfig.getSecretKey()).thenReturn("test_secret_key");
        when(tossPaymentsConfig.getApiUrl()).thenReturn("https://api.tosspayments.com/v1/payments");

        // RestTemplate 응답 Mocking
        ResponseEntity<Map> mockApiResponse = new ResponseEntity<>(Map.of("status", "CANCELED"), HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq("https://api.tosspayments.com/v1/payments/" + TEST_PAYMENT_KEY + "/cancel"), // 정확한 URL
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(mockApiResponse);

        paymentService.cancelPayment(TEST_PAYMENT_ID, TEST_MEMBER_ID, "단순 변심");

        // Payment와 Purchase 상태 변경 확인
        verify(paymentRepository, times(1)).save(testPayment);
        verify(purchaseRepository, times(1)).save(testPurchase);
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(testPurchase.getStatus()).isEqualTo(PurchaseStatus.REJECTED);
    }

    @Test
    @DisplayName("ID로 결제 조회 성공")
    void getPaymentById_Success() {
        // getPaymentById는 pgTid가 업데이트된 Payment를 반환할 수 있도록 설정
        testPayment.updatePgTid(TEST_PAYMENT_KEY);
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPaymentById(TEST_PAYMENT_ID);

        assertThat(response.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(response.getPgTid()).isEqualTo(TEST_PAYMENT_KEY);
    }

    @Test
    @DisplayName("회원 ID로 결제 목록 조회 성공")
    void getPaymentsByMemberId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        // 목록 조회 시 pgTid가 업데이트된 Payment를 반환할 수 있도록 설정
        testPayment.updatePgTid(TEST_PAYMENT_KEY);
        List<Payment> payments = Collections.singletonList(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findByPurchase_MemberId(TEST_MEMBER_ID, pageable)).thenReturn(paymentPage);

        Page<PaymentResponse> result = paymentService.getPaymentsByMemberId(TEST_MEMBER_ID, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(result.getContent().get(0).getPgTid()).isEqualTo(TEST_PAYMENT_KEY);
    }
}
