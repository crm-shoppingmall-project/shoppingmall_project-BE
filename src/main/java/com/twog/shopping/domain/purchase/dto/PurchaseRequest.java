package com.twog.shopping.domain.purchase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    @NotNull(message = "총 금액은 필수입니다.")
    private Integer totalAmount; // BigDecimal -> Integer

    @NotEmpty(message = "주문 상품 목록은 비어있을 수 없습니다.")
    private List<PurchaseItemDto> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemDto {

        @NotNull
        private Long productId;

        @NotNull
        private Integer quantity;

        @NotNull
        private Integer price; // BigDecimal -> Integer
    }
}