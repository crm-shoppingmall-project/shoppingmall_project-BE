package com.twog.shopping.domain.purchase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class PurchaseRequest {

    @NotNull(message = "총 금액은 필수입니다.")
    private BigDecimal totalAmount;

    @NotEmpty(message = "주문 상품 목록은 비어있을 수 없습니다.")
    private List<PurchaseItemDto> items;

    @Getter
    public static class PurchaseItemDto {

        @NotNull
        private Long productId;

        @NotNull
        private Integer quantity;

        @NotNull
        private BigDecimal price;
    }
}