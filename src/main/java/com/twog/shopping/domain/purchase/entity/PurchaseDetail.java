package com.twog.shopping.domain.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
@Table(name = "purchase_detail")
public class PurchaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_detail_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @Column(name = "product_quantity", nullable = false)
    private Integer quantity;

    @Column(name = "purchase_paid_amount", nullable = false)
    private int paidAmount; // BigDecimal -> int

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }
}