package com.twog.shopping.domain.payment.dto;

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
}
