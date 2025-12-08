package com.twog.shopping.domain.payment.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Long memberId;
    private Long purchaseId;
    private Long paymentId;
    private Purchase purchase;
    private Payment payment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        purchaseId = 100L;
        paymentId = 1L;

        // PurchaseDetail 생성
        PurchaseDetail detail1 = PurchaseDetail.builder()
                .productId(1L)
                .quantity(2)
                .paidAmount(10000)
                .build();
        PurchaseDetail detail2 = PurchaseDetail.builder()
                .productId(2L)
                .quantity(1)
                .paidAmount(5000)
                .build();

        // Purchase 엔티티 생성
        purchase = Purchase.builder()
                .id(purchaseId)
                .memberId(memberId)
                .status(PurchaseStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        purchase.addDetail(detail1);
        purchase.addDetail(detail2);

        // Payment 엔티티 생성 (초기 상태는 REQUESTED로 가정)
        payment = Payment.builder()
                .id(paymentId)
                .purchase(purchase)
                .pgTid("temp_pg_tid_123") // 초기에는 임시값
                .status(PaymentStatus.REQUESTED)
                .type(PaymentType.PAYMENT)
                .paidAt(null)
                .createdAt(LocalDateTime.now())
                .build();

        // PaymentRequest 생성
        paymentRequest = new PaymentRequest(
                purchaseId,
                25000,
                PaymentType.PAYMENT,
                "payment_key_abc"
        );
    }

    @Test
    @DisplayName("결제 생성 성공 - 초기 상태는 REQUESTED")
    void createPayment_Success_InitialStatusRequested() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(paymentRepository.findByPurchase_Id(purchaseId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);

        Long createdPaymentId = paymentService.createPayment(paymentRequest, memberId);

        assertThat(createdPaymentId).isEqualTo(paymentId);
        verify(purchaseRepository, times(1)).findById(purchaseId);
        verify(paymentRepository, times(1)).findByPurchase_Id(purchaseId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.REQUESTED);
    }

    @Test
    @DisplayName("결제 생성 실패 - 구매 정보를 찾을 수 없음")
    void createPayment_PurchaseNotFound_Failure() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> paymentService.createPayment(paymentRequest, memberId));

        verify(purchaseRepository, times(1)).findById(purchaseId);
        verify(paymentRepository, never()).findByPurchase_Id(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 생성 실패 - 구매자와 결제 요청자 불일치")
    void createPayment_MemberMismatch_Failure() {
        Long anotherMemberId = 2L;
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThrows(SecurityException.class, () -> paymentService.createPayment(paymentRequest, anotherMemberId));

        verify(purchaseRepository, times(1)).findById(purchaseId);
        verify(paymentRepository, never()).findByPurchase_Id(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 생성 실패 - 이미 결제된 구매")
    void createPayment_AlreadyPaid_Failure() {
        Payment existingPayment = Payment.builder()
                .id(2L)
                .purchase(purchase)
                .pgTid("existing_pg_tid")
                .status(PaymentStatus.COMPLETED)
                .type(PaymentType.PAYMENT)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(paymentRepository.findByPurchase_Id(purchaseId)).thenReturn(Optional.of(existingPayment));

        assertThrows(IllegalStateException.class, () -> paymentService.createPayment(paymentRequest, memberId));

        verify(purchaseRepository, times(1)).findById(purchaseId);
        verify(paymentRepository, times(1)).findByPurchase_Id(purchaseId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 생성 실패 - 결제 금액 불일치")
    void createPayment_AmountMismatch_Failure() {
        PaymentRequest wrongAmountRequest = new PaymentRequest(
                purchaseId,
                10000, // 실제 금액과 다름
                PaymentType.PAYMENT,
                "payment_key_abc"
        );

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(paymentRepository.findByPurchase_Id(purchaseId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> paymentService.createPayment(wrongAmountRequest, memberId));

        verify(purchaseRepository, times(1)).findById(purchaseId);
        verify(paymentRepository, times(1)).findByPurchase_Id(purchaseId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPayment_Success() {
        String newPgTid = "confirmed_pg_tid_456";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment)); // payment는 REQUESTED 상태
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);

        paymentService.confirmPayment(paymentId, newPgTid);

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(payment);
        verify(purchaseRepository, times(1)).save(purchase);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPgTid()).isEqualTo(newPgTid);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.COMPLETED);
    }

    @Test
    @DisplayName("결제 승인 실패 - 결제 정보를 찾을 수 없음")
    void confirmPayment_PaymentNotFound_Failure() {
        String newPgTid = "confirmed_pg_tid_456";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> paymentService.confirmPayment(paymentId, newPgTid));

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    @DisplayName("결제 승인 실패 - 결제 상태가 REQUESTED가 아님")
    void confirmPayment_InvalidStatus_Failure() {
        String newPgTid = "confirmed_pg_tid_456";

        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> paymentService.confirmPayment(paymentId, newPgTid));

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    @DisplayName("ID로 결제 조회 성공")
    void getPaymentById_Success() {

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(paymentId);

        assertThat(response.getPaymentId()).isEqualTo(paymentId);
        assertThat(response.getPurchaseId()).isEqualTo(purchaseId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    @DisplayName("ID로 결제 조회 실패 - 결제 정보를 찾을 수 없음")
    void getPaymentById_NotFound_Failure() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> paymentService.getPaymentById(paymentId));

        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    @DisplayName("회원 ID로 결제 목록 조회 성공")
    void getPaymentsByMemberId_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        List<Payment> payments = Arrays.asList(payment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findByPurchase_MemberId(memberId, pageable)).thenReturn(paymentPage);

        Page<PaymentResponse> responsePage = paymentService.getPaymentsByMemberId(memberId, pageable);

        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getContent().get(0).getPaymentId()).isEqualTo(paymentId);
        verify(paymentRepository, times(1)).findByPurchase_MemberId(memberId, pageable);
    }

    @Test
    @DisplayName("회원 ID로 결제 목록 조회 성공 - 결과 없음")
    void getPaymentsByMemberId_NoResult_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(paymentRepository.findByPurchase_MemberId(memberId, pageable)).thenReturn(emptyPage);

        Page<PaymentResponse> responsePage = paymentService.getPaymentsByMemberId(memberId, pageable);

        assertThat(responsePage.getTotalElements()).isEqualTo(0);
        assertThat(responsePage.getContent()).isEmpty();
        verify(paymentRepository, times(1)).findByPurchase_MemberId(memberId, pageable);
    }

    @Test
    @DisplayName("결제 상태 업데이트 성공")
    void updatePaymentStatus_Success() {
        Payment initialPayment = Payment.builder()
                .id(paymentId)
                .purchase(purchase)
                .pgTid("pg_tid_123")
                .status(PaymentStatus.COMPLETED) // 초기 상태는 COMPLETED로 가정
                .type(PaymentType.PAYMENT) // PaymentType.CARD 대신 PaymentType.PAYMENT 사용
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(initialPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(initialPayment);

        PaymentStatus newStatus = PaymentStatus.REJECTED;
        paymentService.updatePaymentStatus(paymentId, newStatus);

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(initialPayment);
        assertThat(initialPayment.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("결제 상태 업데이트 실패 - 결제 정보를 찾을 수 없음")
    void updatePaymentStatus_NotFound_Failure() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.REJECTED));

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
