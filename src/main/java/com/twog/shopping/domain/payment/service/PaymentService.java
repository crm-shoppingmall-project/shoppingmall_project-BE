package com.twog.shopping.domain.payment.service;

import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.entity.PaymentType;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PurchaseService purchaseService;
    // private final TossPaymentClient tossClient;

    @Transactional
    public void approvePayment(Long purchaseId) {
        Purchase purchase = purchaseService.findById(purchaseId);

        BigDecimal amount = purchaseService.calculateTotalAmount(purchaseId);

        if (purchase.getStatus() != PurchaseStatus.REQUESTED) {
            throw new IllegalStateException("주문 ID : " + purchaseId +" 결제 요청 상태(" + PaymentStatus.REQUESTED + ")가 아니므로 결제를 진행할 수 없습니다. 현재 상태: " + purchase.getStatus());
        }
            String pgTid = "TID_SUCCESS_" + amount.toString();

            Payment payment = Payment.builder()
                    .purchaseId(purchaseId)
                    .pgTid(pgTid)
                    .status(PaymentStatus.COMPLETED)
                    .type(PaymentType.PAYMENT)
                    .paidAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);

            purchase.updateStatus(PurchaseStatus.COMPLETED);
    }
    @Transactional
    public void cancelPayment(Long purchaseId, String reason){
        Purchase purchase = purchaseService.findById(purchaseId);

        if (purchase.getStatus() != PurchaseStatus.COMPLETED) {
            throw new IllegalStateException("주문 ID : " + purchaseId +"결제 완료 상태가 아니므로 환불할 수가 없습니다. 현재 상태: " + purchase.getStatus());
        }
        BigDecimal amount = purchaseService.calculateTotalAmount(purchaseId);
        System.out.println("LOG: purchase ID " + purchaseId + " 에 대해 " + amount + "원 환불 요청. 사유: " + reason);
        String pgTid = "TID_CANCEL_" +purchaseId;

        Payment refundPayment = Payment.builder()
                .purchaseId(purchaseId)
                .pgTid(pgTid)
                .status(PaymentStatus.REJECTED)
                .type(PaymentType.REFUND)
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(refundPayment);

        purchase.updateStatus(PurchaseStatus.REJECTED);
    }
}
