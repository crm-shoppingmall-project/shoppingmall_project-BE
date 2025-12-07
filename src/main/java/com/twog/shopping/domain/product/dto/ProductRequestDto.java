package com.twog.shopping.domain.product.dto;

import com.twog.shopping.domain.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    private String productName;
    private String productCategory;
    private int productPrice;
    private int productQuantity;
    private ProductStatus productStatus;
}
