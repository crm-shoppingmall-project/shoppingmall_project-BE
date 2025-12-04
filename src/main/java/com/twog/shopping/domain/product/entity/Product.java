package com.twog.shopping.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "entityProduct")
@Table(name = "product")
@Getter
@Setter
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String productName;
    private String productCategory;
    private java.sql.Timestamp productRegisted;
    private Integer productQuantity;
    private Integer productPrice;
    private ProductStatus productStatus;
    private java.sql.Timestamp productUpdated;

}
