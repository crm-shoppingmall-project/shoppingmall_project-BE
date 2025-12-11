package com.twog.shopping.domain.cart.entity;

import com.twog.shopping.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Cart_item")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @UpdateTimestamp
    @Column(name = "cart_item_updated")
    private LocalDateTime cartItemUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_item_status")
    private CartItemStatus cartItemStatus;

    @Column(name = "cart_item_quantity")
    private int cartItemQuantity;

    public static CartItem createCartItem(Cart cart, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.cart = cart;
        cartItem.product = product;
        cartItem.cartItemQuantity = quantity;
        cartItem.cartItemStatus = CartItemStatus.ACTIVE;
        return cartItem;
    }

    // --- 비즈니스 로직 ---

    public void addQuantity(int quantity) {
        this.cartItemQuantity += quantity;
    }

    public void removeQuantity(int quantity) {
        this.cartItemQuantity -= quantity;
    }

    public void updateQuantity(int quantity) {
        this.cartItemQuantity = quantity;
    }

    public void updateCartItemStatus(CartItemStatus cartItemStatus) {
        this.cartItemStatus = cartItemStatus;
    }

    // 삭제된 아이템 복구 로직
    public void reactivate() {
        if (this.cartItemStatus == CartItemStatus.REMOVED) {
            this.cartItemStatus = CartItemStatus.ACTIVE;
        }
    }

}
