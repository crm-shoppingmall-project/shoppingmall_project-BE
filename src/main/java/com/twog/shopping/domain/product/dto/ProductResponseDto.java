package com.twog.shopping.domain.product.dto;

import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import lombok.Getter;

@Getter
public class ProductResponseDto {
    private final int productId;
    private final String productName;
    private final String productCategory;
    private final int productPrice;
    private final int productQuantity;
    private final ProductStatus productStatus;

    public ProductResponseDto(Product product) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
        this.productCategory = product.getProductCategory();
        this.productPrice = product.getProductPrice();
        this.productQuantity = product.getProductQuantity();
        this.productStatus = product.getProductStatus();
    }
}
