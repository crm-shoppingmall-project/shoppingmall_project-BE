package com.twog.shopping.domain.purchase.controller;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final MemberRepository memberRepository;

    /**
     * 상품 주문 생성
     * [POST] /api/v1/purchases
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPurchase(
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal User user) {

        Member member = findMemberByUser(user);
        Long purchaseId = purchaseService.createPurchase(request, member.getMemberId());

        Map<String, Object> responseBody = Map.of(
                "status", "CREATED",
                "message", "주문이 성공적으로 생성되었습니다.",
                "data", purchaseId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    /**
     * 내 주문 목록 조회
     * [GET] /api/v1/purchases
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyPurchases(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        Member member = findMemberByUser(user);
        Page<PurchaseResponse> purchases = purchaseService.findMyPurchases(member.getMemberId(), pageable);

        Map<String, Object> responseBody = Map.of(
                "status", "OK",
                "message", "주문 목록 조회가 완료되었습니다.",
                "data", purchases
        );
        return ResponseEntity.ok(responseBody);
    }

    /**
     * 주문 취소
     * [POST] /api/v1/purchases/{id}/cancel
     */
    @PostMapping("/{purchaseId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelPurchase(
            @PathVariable Long purchaseId,
            @AuthenticationPrincipal User user) {

        Member member = findMemberByUser(user);
        purchaseService.cancelPurchase(purchaseId, member.getMemberId());

        Map<String, Object> responseBody = Map.of(
                "status", "OK",
                "message", "주문이 성공적으로 취소되었습니다."
        );
        return ResponseEntity.ok(responseBody);
    }

    private Member findMemberByUser(User user) {
        String userEmail = user.getUsername();
        return memberRepository.findByMemberEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + userEmail));
    }
}