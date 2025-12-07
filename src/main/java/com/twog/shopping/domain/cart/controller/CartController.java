package com.twog.shopping.domain.cart.controller;

import com.twog.shopping.domain.cart.dto.CartDetailDto;
import com.twog.shopping.domain.cart.dto.CartItemRequestDto;
import com.twog.shopping.domain.member.service.DetailsUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.twog.shopping.domain.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/details")
    public ResponseEntity<List<CartDetailDto>> getCartDetails(@AuthenticationPrincipal DetailsUser user) {
        List<CartDetailDto> cartDetails = cartService.getCartDetails(user.getMember().getMemberId().intValue());
        return ResponseEntity.ok(cartDetails);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart(@AuthenticationPrincipal DetailsUser user,
            @RequestBody CartItemRequestDto requestDto) {
        cartService.addItemToCart(user.getMember().getMemberId().intValue(), requestDto.getProductId(),
                requestDto.getQuantity());
        return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCartItemQuantity(@AuthenticationPrincipal DetailsUser user,
            @RequestBody CartItemRequestDto requestDto) {
        cartService.updateCartItemQuantity(user.getMember().getMemberId().intValue(), requestDto.getProductId(),
                requestDto.getQuantity());
        return ResponseEntity.ok("장바구니 상품 수량이 업데이트되었습니다.");
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeItemFromCart(@AuthenticationPrincipal DetailsUser user,
            @PathVariable int productId) {
        cartService.removeItemFromCart(user.getMember().getMemberId().intValue(), productId);
        return ResponseEntity.ok("장바구니에서 상품이 제거되었습니다.");
    }

    @PutMapping("/update-all")
    public ResponseEntity<String> updateAllCartItems(@AuthenticationPrincipal DetailsUser user,
            @RequestBody List<CartDetailDto> cartDetails) {
        cartService.updateCartItems(user.getMember().getMemberId().intValue(), cartDetails);
        return ResponseEntity.ok("장바구니 상품들이 일괄 업데이트되었습니다.");
    }
}
