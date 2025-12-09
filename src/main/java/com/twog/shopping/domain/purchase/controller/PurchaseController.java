package com.twog.shopping.domain.purchase.controller;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.member.service.MemberService;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.service.PurchaseService;
import com.twog.shopping.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final MemberService memberService;

    /**
     * 상품 주문 생성
     * [POST] /api/v1/purchases
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseResponse>> createPurchase(
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal DetailsUser user) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success(HttpStatus.CREATED, user.toString()));
        Member member = memberService.getByEmailOrThrow(user.getUsername());
//        Long purchaseId = purchaseService.createPurchase(request, member.getMemberId());
        Purchase purchase = purchaseService.createPurchase(request, member.getMemberId());

        PurchaseResponse response = PurchaseResponse.fromEntity(purchase);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "주문이 성공적으로 생성되었습니다.", response));
    }



    /**
     * 장바구니 상품으로 주문 생성
     * [POST] /api/v1/purchases/from-cart
     */
    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<Long>> createPurchaseFromCart(
            @AuthenticationPrincipal User user) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        Long purchaseId = purchaseService.createPurchaseFromCart(member.getMemberId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "장바구니 상품으로 주문이 성공적으로 생성되었습니다.", purchaseId));
    }

    /**
     * 내 주문 목록 조회
     * [GET] /api/v1/purchases
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PurchaseResponse>>> getMyPurchases(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        Page<PurchaseResponse> purchases = purchaseService.findMyPurchases(member.getMemberId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "주문 목록 조회가 완료되었습니다.", purchases));
    }

    /**
     * 주문 취소
     * [POST] /api/v1/purchases/{purchaseId}/cancel
     */
    @PostMapping("/{purchaseId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPurchase(
            @PathVariable Long purchaseId,
            @AuthenticationPrincipal User user) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        purchaseService.cancelPurchase(purchaseId, member.getMemberId());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "주문이 성공적으로 취소되었습니다."));
    }
}
