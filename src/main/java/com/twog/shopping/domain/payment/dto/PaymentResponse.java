package com.twog.shopping.domain.payment.dto;

<<<<<<< HEAD
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .purchaseId(payment.getPurchase().getId())
                .pgTid(payment.getPgTid())
                .status(payment.getStatus())
                .type(payment.getType())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
=======
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {

    private String status;

    private  String message;

    private Long purchaseId;

    private Long amount;

    private String pgTid;

    private String paymentKey;
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
}
