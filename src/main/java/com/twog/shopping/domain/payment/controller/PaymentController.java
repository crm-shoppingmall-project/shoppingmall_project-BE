package com.twog.shopping.domain.payment.controller;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.member.service.MemberService;
import com.twog.shopping.domain.payment.dto.PaymentRequest;
import com.twog.shopping.domain.payment.dto.PaymentResponse;
import com.twog.shopping.domain.payment.service.PaymentService;
import com.twog.shopping.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "Payment API", description = "결제 요청, 승인, 취소, 조회 관련 API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberService memberService;

    @Operation(summary = "결제 초기화 (결제 요청)", description = "주문 정보를 바탕으로 결제를 요청하고, 결제 ID를 생성합니다.")
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal DetailsUser user) {

        Member member = memberService.getByEmailOrThrow(user.getUsername());
        Long paymentId = paymentService.initiatePayment(request, member.getMemberId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "결제 요청이 성공적으로 생성되었습니다.", Map.of("paymentId", paymentId)));
    }

    @Operation(summary = "토스페이먼츠 결제 승인", description = "토스페이먼츠의 결제 성공 후 콜백을 받아 결제를 최종 승인 처리합니다.")
    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmTossPayment(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount) {

        paymentService.confirmTossPayment(paymentKey, orderId, amount);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "결제가 성공적으로 승인되었습니다."));
    }

    @Operation(summary = "결제 취소 (환불)", description = "특정 결제를 취소하고 환불을 요청합니다.")
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal DetailsUser user) {

        Optional<Member> member = memberService.findByEmail(user.getUsername());
        String cancelReason = requestBody.getOrDefault("cancelReason", "고객 요청");
        paymentService.cancelPayment(paymentId, member.get().getMemberId(), cancelReason);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "결제가 성공적으로 취소되었습니다."));
    }

    @Operation(summary = "특정 결제 정보 조회", description = "결제 ID를 통해 특정 결제 내역을 상세 조회합니다.")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal DetailsUser user) {

        PaymentResponse paymentResponse = paymentService.getPaymentById(paymentId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "결제 정보 조회가 완료되었습니다.", paymentResponse));
    }
}
