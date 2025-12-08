package com.twog.shopping.domain.product.entity;

import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.global.error.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Product")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_category")
    private String productCategory;

    @CreationTimestamp
    @Column(name = "product_registed", updatable = false)
    private LocalDateTime productRegisted;

    @Column(name = "product_quantity")
    private int productQuantity;

    @Column(name = "product_price")
    private int productPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status")
    private ProductStatus productStatus;

    @UpdateTimestamp
    @Column(name = "product_updated")
    private LocalDateTime productUpdated;

    @Version
    private Long version;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private List<CartItem> cartItems = new ArrayList<>();

    /*
     * // To-do: PurchaseDetail과 매핑 문제 해결 필요
     *
     * @OneToMany(mappedBy = "product")
     *
     * @ToString.Exclude
     * private List<PurchaseDetail> purchaseDetails = new ArrayList<>();
     */

    @Builder
    public Product(String productName, String productCategory, int productQuantity, int productPrice,
            ProductStatus productStatus) {
        this.productName = productName;
        this.productCategory = productCategory;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.productStatus = productStatus;
    }

    // 비지니스 로직

    public void updateProductInfo(String productName, String productCategory, int productQuantity, int productPrice,
            ProductStatus productStatus) {
        this.productName = productName;
        this.productCategory = productCategory;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.productStatus = productStatus;
    }

    // 재고 감소 로직
    public void decreaseStock(int quantity) {
        int restStock = this.productQuantity - quantity;
        if (restStock < 0) {
            throw new OutOfStockException("재고가 부족합니다. (현재 재고: " + this.productQuantity + ")");
        }
        this.productQuantity = restStock;
    }

    public boolean isStock(int quantity) {
        return this.productQuantity >= quantity;
    }

    // 논리적 삭제 로직
    public void delete() {
        this.productStatus = ProductStatus.DELETED;
    }
}