package com.twog.shopping.domain.payment.dto;

import com.twog.shopping.domain.payment.entity.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "구매 ID는 필수입니다.")
    private Long purchaseId;

    @NotNull(message = "결제 금액은 필수입니다.")
    private Integer amount;

    @NotNull(message = "결제 타입은 필수입니다.")
    private PaymentType paymentType;

    @NotNull(message = "결제 키는 필수입니다.")
    private String paymentKey;
}
