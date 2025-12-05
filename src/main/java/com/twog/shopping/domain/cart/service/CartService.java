package com.twog.shopping.domain.cart.service;

import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.entity.CartItemStatus;
import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository; // MemberRepository import
import com.twog.shopping.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository; // MemberRepository 주입

    @Transactional
    public Cart findOrCreateCartByMemberId(int memberId) {
        return cartRepository.findByMember_MemberId(memberId)
            .orElseGet(() -> {
                Member member = memberRepository.findById((long) memberId)
                        .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
                Cart newCart = Cart.createCart(member);
                return cartRepository.save(newCart);
            });
    }

    @Transactional
    public void deleteCartByMemberId(int memberId) { // NOTI: 실제 데이터 삭제이므로 호출 주의!
        // memberId로 Cart를 찾아서, 존재할 경우에만 실제 Cart & CartItem 연관 데이터 모두 delete 메서드 실행
        cartRepository.findByMember_MemberId(memberId).ifPresent(cartRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByMemberId(int memberId) {
        Cart cart = cartRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new RuntimeException("해당 회원의 장바구니를 찾을 수 없습니다."));
        return cart.getCartItems();
    }

    @Transactional(readOnly = true)
    public void printCartItemsByMemberId(int memberId) {
        List<CartItem> cartItems = getCartItemsByMemberId(memberId);

        System.out.println("회원 ID:" + memberId + " 장바구니 목록");
        System.out.println("==================================");

        if (cartItems.isEmpty()) {
            System.out.println("장바구니에 상품이 없습니다.");
            return;
        }

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            System.out.println("상품 정보: " + product.toString());
            System.out.println("------------------------------");
        }
    }

    @Transactional(readOnly = true)
    public List<CartItem> getActiveCartItemsByMemberId(int memberId) {
        List<CartItemStatus> statuses = List.of(CartItemStatus.ACTIVE, CartItemStatus.UPDATED);

        return cartItemRepository.findCartItemsByMemberIdAndStatus(memberId, statuses);
    }
}
