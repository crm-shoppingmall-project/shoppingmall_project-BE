package com.twog.shopping.domain.payment.dto;

<<<<<<< HEAD
import com.twog.shopping.domain.payment.entity.PaymentType;
import jakarta.validation.constraints.NotNull;
=======
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

<<<<<<< HEAD
    @NotNull(message = "구매 ID는 필수입니다.")
    private Long purchaseId;

    @NotNull(message = "결제 금액은 필수입니다.")
    private Integer amount;

    @NotNull(message = "결제 타입은 필수입니다.")
    private PaymentType paymentType;

    @NotNull(message = "결제 키는 필수입니다.")
    private String paymentKey;
=======
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long purchaseId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 100, message = "최소 결제 금액은 100원 이상이어야 합니다.")
    private Long amount;

    @NotNull(message = "주문명은 필수입니다.")
    @Size(min = 1, max = 100, message = "주문명은 1자 이상 100자 이하로 입력해야 합니다.")
    private String purchaseName;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "성공 콜백 URL은 필수입니다.")
    private String successUrl;

    @NotNull(message = "실패 콜백 URL은 필수입니다.")
    private String failUrl;

    private String customerName;

    private String metadata;
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
}
