package com.twog.shopping.domain.cart.controller;

import com.twog.shopping.domain.cart.dto.CartDetailDto;
import com.twog.shopping.domain.cart.dto.CartItemRequestDto;
import com.twog.shopping.domain.member.service.DetailsUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.twog.shopping.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/details")
    @Operation(summary = "장바구니 목록 조회", description = "로그인한 사용자의 장바구니에 담긴 상품 목록을 조회합니다.")
    public ResponseEntity<List<CartDetailDto>> getCartDetails(@AuthenticationPrincipal DetailsUser user) {
        List<CartDetailDto> cartDetails = cartService.getCartDetails(user.getMember().getMemberId().intValue());
        return ResponseEntity.ok(cartDetails);
    }

    @PostMapping("/add")
    @Operation(summary = "장바구니 상품 추가", description = "장바구니에 새로운 상품을 추가합니다.")
    public ResponseEntity<String> addItemToCart(@AuthenticationPrincipal DetailsUser user,
            @RequestBody CartItemRequestDto requestDto) {
        cartService.addItemToCart(user.getMember().getMemberId().intValue(), requestDto.getProductId(),
                requestDto.getQuantity());
        return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
    }

    @PutMapping("/update")
    @Operation(summary = "장바구니 상품 수량 수정", description = "장바구니에 담긴 특정 상품의 수량을 수정합니다.")
    public ResponseEntity<String> updateCartItemQuantity(@AuthenticationPrincipal DetailsUser user,
            @RequestBody CartItemRequestDto requestDto) {
        cartService.updateCartItemQuantity(user.getMember().getMemberId().intValue(), requestDto.getProductId(),
                requestDto.getQuantity());
        return ResponseEntity.ok("장바구니 상품 수량이 업데이트되었습니다.");
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다.")
    public ResponseEntity<String> removeItemFromCart(@AuthenticationPrincipal DetailsUser user,
            @PathVariable int productId) {
        cartService.removeItemFromCart(user.getMember().getMemberId().intValue(), productId);
        return ResponseEntity.ok("장바구니에서 상품이 제거되었습니다.");
    }

    @PutMapping("/update-all")
    @Operation(summary = "장바구니 상품 일괄 수정", description = "장바구니의 여러 상품 정보를 일괄적으로 수정합니다.")
    public ResponseEntity<String> updateAllCartItems(@AuthenticationPrincipal DetailsUser user,
            @RequestBody List<CartDetailDto> cartDetails) {
        cartService.updateCartItems(user.getMember().getMemberId().intValue(), cartDetails);
        return ResponseEntity.ok("장바구니 상품들이 일괄 업데이트되었습니다.");
    }
}
