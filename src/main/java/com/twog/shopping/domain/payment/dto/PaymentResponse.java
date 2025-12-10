package com.twog.shopping.domain.payment.dto;

import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.entity.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long purchaseId;
    private String pgTid;
    private PaymentStatus status;
    private PaymentType type;
    private LocalDateTime paidAt;
    private LocalDateTime paymentCreated;

    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .purchaseId(payment.getPurchase().getId())
                .pgTid(payment.getPgTid())
                .status(payment.getStatus())
                .type(payment.getType())
                .paidAt(payment.getPaidAt())
                .paymentCreated(payment.getPaymentCreated())
                .build();
    }
}
