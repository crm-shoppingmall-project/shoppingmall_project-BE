package com.twog.shopping.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartDetailDto {
    private int cartItemId;
    private int productId;
    private String productName;
    private int productPrice;
    private int cartQuantity;
    private int productStock; // 현재 실제 재고

    // 프론트가 처리하기 위한 코드
    private boolean isOrderable; // cartQuantity <= productStock
    private String alertMessage; // Ex) 재고가 부족하여 최대 2개만 주문 가능합니다.
}
