package com.twog.shopping.domain.payment.service;

import com.twog.shopping.domain.payment.dto.PaymentRequest;
import com.twog.shopping.domain.payment.dto.PaymentResponse;
import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.entity.PaymentType;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public Long createPayment(PaymentRequest request, Long memberId) {
        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new NoSuchElementException("구매 정보를 찾을 수 없습니다. (ID: " + request.getPurchaseId() + ")"));

        if (!Objects.equals(purchase.getMemberId(), memberId)) {
            throw new SecurityException("해당 구매에 대한 결제 권한이 없습니다.");
        }

        if (paymentRepository.findByPurchase_Id(purchase.getId()).isPresent()) {
            throw new IllegalStateException("이미 결제가 완료된 구매입니다. (구매 ID: " + purchase.getId() + ")");
        }

        Integer actualPurchaseAmount = purchase.getDetails().stream()
                .mapToInt(detail -> detail.getPaidAmount() * detail.getQuantity())
                .sum();

        if (!Objects.equals(actualPurchaseAmount, request.getAmount())) {
            throw new IllegalStateException("결제 금액이 구매 금액과 일치하지 않습니다. (요청 금액: " + request.getAmount() + ", 실제 구매 금액: " + actualPurchaseAmount + ")");
        }

        // 초기 결제 상태를 REQUESTED로 설정
        Payment payment = Payment.builder()
                .purchase(purchase)
                .pgTid(request.getPaymentKey()) // PG사 거래번호 (초기에는 임시값 또는 요청값)
                .status(PaymentStatus.REQUESTED) // 결제 요청 상태
                .type(request.getPaymentType())
                .paidAt(null) // 아직 결제 완료되지 않았으므로 null
                .build();

        paymentRepository.save(payment);

        // 구매 상태를 결제 요청 상태로 변경 (선택 사항, REQUESTED가 더 적절할 수 있음)
        // purchase.updateStatus(PurchaseStatus.PAYMENT_REQUESTED); // 만약 PurchaseStatus에 PAYMENT_REQUESTED가 있다면
        purchaseRepository.save(purchase); // purchase 엔티티의 변경사항 저장 (여기서는 상태 변경이 없으므로 생략 가능하지만, 일관성을 위해 유지)

        return payment.getId();
    }

    @Transactional
    public void confirmPayment(Long paymentId, String pgTid) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (ID: " + paymentId + ")"));

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            throw new IllegalStateException("결제 승인 대기 상태가 아닙니다. (현재 상태: " + payment.getStatus() + ")");
        }

        // PG사로부터 받은 최종 pgTid로 업데이트
        payment.updatePgTid(pgTid); // Payment 엔티티에 updatePgTid 메서드가 필요
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now()); // 결제 완료 시간 설정
        paymentRepository.save(payment);

        // 구매 상태를 결제 완료로 변경
        Purchase purchase = payment.getPurchase();
        purchase.updateStatus(PurchaseStatus.COMPLETED);
        purchaseRepository.save(purchase);
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (ID: " + paymentId + ")"));
        return PaymentResponse.fromEntity(payment);
    }

    public Page<PaymentResponse> getPaymentsByMemberId(Long memberId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByPurchase_MemberId(memberId, pageable);
        return payments.map(PaymentResponse::fromEntity);
    }

    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (ID: " + paymentId + ")"));
        payment.setStatus(newStatus);
        paymentRepository.save(payment);
    }
}
