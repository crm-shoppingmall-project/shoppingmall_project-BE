package com.twog.shopping.domain.cart.entity;

import com.twog.shopping.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Entity
@Table(name = "Cart_item")
@Getter
@Setter
@ToString
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private int cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "cart_item_updated")
    private Timestamp cartItemUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_item_status")
    private CartItemStatus cartItemStatus;

    @Column(name = "cart_item_quantity")
    private int cartItemQuantity;

}
