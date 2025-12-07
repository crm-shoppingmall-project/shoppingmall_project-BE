package com.twog.shopping.domain.cart.entity;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Cart")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private int cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CartItem> cartItems = new ArrayList<>();

    private Cart(Member member) {
        this.member = member;
    }

    public static Cart createCart(Member member) {
        return new Cart(member);
    }

    // --- 비즈니스 로직 (DDD) ---

    // 상품 추가 (기존에 있으면 수량 증가, 없으면 생성)
    public void addItem(Product product, int quantity) {
        CartItem item = findCartItem(product);

        if (item != null) {
            // 이미 있는 상품: 수량 증가 체크
            int totalQuantity = item.getCartItemQuantity() + quantity;
            checkStock(product, totalQuantity);
            item.addQuantity(quantity);
            // 상태가 REMOVED였다면 다시 ACTIVE/UPDATED로 살리기 위해 상태 업데이트
            item.reactivate();
        } else {
            // 없는 상품의 경우, 생성 체크
            checkStock(product, quantity);
            CartItem newItem = CartItem.createCartItem(this, product, quantity);
            this.cartItems.add(newItem);
        }
    }

    // 상품 수량 변경 (덮어쓰기)
    public void updateItemQuantity(Product product, int quantity) {
        CartItem item = findCartItem(product);
        if (item != null) {
            checkStock(product, quantity);
            item.updateQuantity(quantity);
        } else {
            throw new RuntimeException("상품이 존재하지 않습니다.");
        }
    }

    // 상품 삭제 (Soft Delete)
    public void removeItem(Product product) {
        CartItem item = findCartItem(product);
        if (item != null) {
            item.updateCartItemStatus(CartItemStatus.REMOVED);
        }
    }

    // 장바구니에서 유효한(ACTIVE/UPDATED) 상품 찾기
    private CartItem findCartItem(Product product) {
        return this.cartItems.stream()
                .filter(item -> item.getProduct().getProductId() == product.getProductId())
                .filter(item -> item.getCartItemStatus() == CartItemStatus.ACTIVE
                        || item.getCartItemStatus() == CartItemStatus.UPDATED)
                .findFirst()
                .orElse(null);
    }

    // 재고 검증
    private void checkStock(Product product, int quantity) {
        if (!product.isStock(quantity)) {
            throw new RuntimeException("재고가 부족합니다. (남은 수량: " + product.getProductQuantity() + ")");
        }
    }
}
