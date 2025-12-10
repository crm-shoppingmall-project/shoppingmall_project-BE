package com.twog.shopping.domain.purchase.controller;

import com.twog.shopping.domain.log.aop.LogHistory;
import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.member.service.MemberService;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.service.PurchaseService;
import com.twog.shopping.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Purchase API", description = "주문 생성, 조회, 취소 관련 API")
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final MemberService memberService;

    @Operation(summary = "상품 주문 생성", description = "상품 정보를 직접 받아 주문을 생성합니다.")
    @PostMapping
    @LogHistory(actionType = HistoryActionType.PURCHASE_COMPLETED)
    public ResponseEntity<ApiResponse<PurchaseResponse>> createPurchase(
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal DetailsUser user) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        Purchase purchase = purchaseService.createPurchase(request, member.getMemberId());
        PurchaseResponse response = PurchaseResponse.fromEntity(purchase);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "주문이 성공적으로 생성되었습니다.", response));
    }

    @Operation(summary = "장바구니 상품으로 주문 생성", description = "현재 사용자의 장바구니에 담긴 모든 상품으로 주문을 생성합니다.")
    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<Long>> createPurchaseFromCart(
            @AuthenticationPrincipal DetailsUser user) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        Long purchaseId = purchaseService.createPurchaseFromCart(member.getMemberId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "장바구니 상품으로 주문이 성공적으로 생성되었습니다.", purchaseId));
    }

    @Operation(summary = "내 주문 목록 조회", description = "인증된 사용자의 주문 내역을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PurchaseResponse>>> getMyPurchases(
            @AuthenticationPrincipal DetailsUser user,
            Pageable pageable) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        Page<PurchaseResponse> purchases = purchaseService.findMyPurchases(member.getMemberId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "주문 목록 조회가 완료되었습니다.", purchases));
    }

    @Operation(summary = "주문 취소", description = "특정 주문을 취소 상태로 변경합니다.")
    @PostMapping("/{purchaseId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPurchase(
            @PathVariable Long purchaseId,
            @AuthenticationPrincipal DetailsUser user) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        purchaseService.cancelPurchase(purchaseId, member.getMemberId());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "주문이 성공적으로 취소되었습니다."));
    }
}
