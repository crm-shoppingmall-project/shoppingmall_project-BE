package com.twog.shopping.domain.cart.service;

import com.twog.shopping.domain.cart.dto.CartDetailDto;
import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.entity.CartItemStatus;
import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.global.exception.InvalidProductStatusException;
import com.twog.shopping.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 사용자 ID로 장바구니를 조회하거나, 없으면 새로 생성
    @Transactional
    public Cart findOrCreateCartByMemberId(int memberId) {
        return cartRepository.findByMember_MemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById((long) memberId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "해당 회원을 찾을 수 없습니다."));
                    Cart newCart = Cart.createCart(member);
                    return cartRepository.save(newCart);
                });
    }

    // 사용자 ID로 장바구니와 포함된 상품들을 삭제
    // NOTI: 실제 데이터 삭제이므로 호출 주의!
    @Transactional
    public void deleteCartByMemberId(int memberId) {
        cartRepository.findByMember_MemberId(memberId).ifPresent(cartRepository::delete);
    }

    // 사용자 ID로 장바구니에 담긴 모든 상품 목록 조회
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByMemberId(int memberId) {
        Cart cart = cartRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "해당 회원의 장바구니를 찾을 수 없습니다."));
        return cart.getCartItems();
    }

    // 사용자 ID로 활성화(ACTIVE) 또는 수정된(UPDATED) 상태의 장바구니 상품 조회
    @Transactional(readOnly = true)
    public List<CartItem> getActiveCartItemsByMemberId(int memberId) {
        List<CartItemStatus> statuses = List.of(CartItemStatus.ACTIVE, CartItemStatus.UPDATED);

        return cartItemRepository.findCartItemsByMemberIdAndStatus(memberId, statuses);
    }

    // 장바구니에 상품을 추가하거나, 이미 존재하면 수량을 증가시킵니다 (재고 체크 포함).
    @Transactional
    public void addItemToCart(int memberId, int productId, int quantity) {
        Cart memberCart = findOrCreateCartByMemberId(memberId);
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("해당 상품을 찾을 수 없습니다."));

        // 상품 상태 검증
        if (product.getProductStatus() == com.twog.shopping.domain.product.entity.ProductStatus.DELETED) {
            throw new InvalidProductStatusException("판매가 중단된 상품입니다.");
        }

        memberCart.addItem(product, quantity);
    }

    // 장바구니에서 특정 상품을 제거(상태 변경)
    @Transactional
    public void removeItemFromCart(int memberId, int productId) {
        Cart memberCart = findOrCreateCartByMemberId(memberId);
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("해당 상품을 찾을 수 없습니다."));

        memberCart.removeItem(product);
    }

    // 장바구니에 담긴 상품의 수량을 변경 (재고 체크 및 상태 변경 포함).
    @Transactional
    public void updateCartItemQuantity(int memberId, int productId, int quantity) {
        Cart memberCart = findOrCreateCartByMemberId(memberId);
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("해당 상품을 찾을 수 없습니다."));

        // 상품 상태 검증 (수량 변경 시에도 유효하지 않은 상품이면 차단)
        if (product.getProductStatus() == com.twog.shopping.domain.product.entity.ProductStatus.DELETED) {
            throw new InvalidProductStatusException("판매가 중단된 상품입니다.");
        }

        memberCart.updateItemQuantity(product, quantity);
    }

    // 장바구니 상세 정보를 조회하여 DTO로 반환 (재고 부족 여부 포함).
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartDetails(int memberId) {
        List<CartItem> cartItems = getActiveCartItemsByMemberId(memberId);

        return cartItems.stream()
                .map(item -> {
                    Product product = item.getProduct();
                    int currentStock = product.getProductQuantity();
                    int cartQuantity = item.getCartItemQuantity();
                    boolean isOrderable = product.isStock(cartQuantity);

                    // 삭제된 상품인 경우 주문 불가 처리
                    if (product.getProductStatus() == com.twog.shopping.domain.product.entity.ProductStatus.DELETED) {
                        isOrderable = false;
                    }

                    String message = null;
                    if (!isOrderable) {
                        if (product
                                .getProductStatus() == com.twog.shopping.domain.product.entity.ProductStatus.DELETED) {
                            message = "판매가 중단된 상품입니다.";
                        } else {
                            message = "재고가 부족하여 주문할 수 없습니다. (현재 재고: " + currentStock + ")";
                        }
                    }

                    return CartDetailDto.builder()
                            .cartItemId(item.getCartItemId())
                            .productId(product.getProductId())
                            .productName(product.getProductName())
                            .productPrice(product.getProductPrice())
                            .cartQuantity(cartQuantity)
                            .productStock(currentStock)
                            .isOrderable(isOrderable)
                            .alertMessage(message)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 장바구니의 모든 상품 수량을 일괄 업데이트
    @Transactional
    public void updateCartItems(int memberId, List<CartDetailDto> cartDetails) {
        Cart cart = findOrCreateCartByMemberId(memberId);

        for (CartDetailDto cartDetail : cartDetails) {
            Product product = productRepository.findById(cartDetail.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "해당 상품을 찾을 수 없습니다."));

            // 상품 상태 검증
            if (product.getProductStatus() == com.twog.shopping.domain.product.entity.ProductStatus.DELETED) {
                throw new InvalidProductStatusException(
                        "판매가 중단된 상품이 포함되어 있습니다. (" + product.getProductName() + ")");
            }

            cart.updateItemQuantity(product, cartDetail.getCartQuantity());
        }
    }
}
