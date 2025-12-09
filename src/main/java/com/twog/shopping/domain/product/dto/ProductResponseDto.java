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
    private final int discountPrice;

    public ProductResponseDto(Product product) {
        this(product, null);
    }

    public ProductResponseDto(Product product, com.twog.shopping.global.common.entity.GradeName gradeName) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
        this.productCategory = product.getProductCategory();
        this.productPrice = product.getProductPrice();
        this.productQuantity = product.getProductQuantity();
        this.productStatus = product.getProductStatus();

        if (gradeName != null) {
            this.discountPrice = gradeName.applyDiscountRate(product.getProductPrice());
        } else {
            this.discountPrice = product.getProductPrice();
        }
    }
}
