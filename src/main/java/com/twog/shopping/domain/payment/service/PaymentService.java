package com.twog.shopping.domain.payment.service;

import com.twog.shopping.domain.payment.dto.PaymentRequest;
import com.twog.shopping.domain.payment.dto.PaymentResponse;
import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.global.config.TossPaymentsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID; // UUID import 추가

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PurchaseRepository purchaseRepository;
    private final TossPaymentsConfig tossPaymentsConfig;
    private final RestTemplate restTemplate;

    @Transactional
    public Long initiatePayment(PaymentRequest request, Long memberId) {
        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new NoSuchElementException("구매 정보를 찾을 수 없습니다. (ID: " + request.getPurchaseId() + ")"));

        if (!Objects.equals(purchase.getMemberId(), memberId)) {
            throw new SecurityException("해당 구매에 대한 결제 권한이 없습니다.");
        }

        if (paymentRepository.findByPurchase_Id(purchase.getId()).isPresent()) {
            throw new IllegalStateException("이미 결제가 완료된 구매입니다. (구매 ID: " + purchase.getId() + ")");
        }

        Integer actualPurchaseAmount = purchase.getDetails().stream()
                .mapToInt(detail -> detail.getPaidAmount() * detail.getQuantity())
                .sum();

        if (!Objects.equals(actualPurchaseAmount, request.getAmount())) {
            throw new IllegalStateException("결제 금액이 구매 금액과 일치하지 않습니다. (요청 금액: " + request.getAmount() + ", 실제 구매 금액: " + actualPurchaseAmount + ")");
        }

        Payment payment = Payment.builder()
                .purchase(purchase)
                // pgTid에 20자 이내의 임시 값 할당
                .pgTid(UUID.randomUUID().toString().substring(0, 20)) // UUID 앞 20자 사용
                .status(PaymentStatus.REQUESTED)
                .type(request.getPaymentType())
                .paidAt(LocalDateTime.now()) // paidAt 필드에 현재 시간 설정
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return savedPayment.getId();
    }

    @Transactional
    public void confirmTossPayment(String paymentKey, String orderId, Integer amount) {

        // findByPurchase_Id 대신 findById를 사용하도록 수정
        Payment payment = paymentRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (OrderId: " + orderId + ")"));

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            throw new IllegalStateException("결제 승인 대기 상태가 아닙니다. (현재 상태: " + payment.getStatus() + ")");
        }

        Purchase purchase = payment.getPurchase();
        Integer actualPurchaseAmount = purchase.getDetails().stream()
                .mapToInt(detail -> detail.getPaidAmount() * detail.getQuantity())
                .sum();

        if (!Objects.equals(actualPurchaseAmount, amount)) {
            throw new IllegalStateException("토스페이먼츠 결제 금액과 구매 금액이 일치하지 않습니다. (토스 금액: " + amount + ", 실제 구매 금액: " + actualPurchaseAmount + ")");
        }

        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossPaymentsConfig.getSecretKey() + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 토스페이먼츠 결제 승인 API 요청 본문
        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 토스페이먼츠 결제 승인 API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossPaymentsConfig.getApiUrl() + "/confirm",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 결제 성공 처리
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.updatePgTid(paymentKey);
                paymentRepository.save(payment);

                purchase.updateStatus(PurchaseStatus.COMPLETED);
                purchaseRepository.save(purchase);
            } else {
                // 결제 실패 처리
                // 필요하다면 payment.setStatus(PaymentStatus.FAILED) 등으로 변경 가능
                throw new RuntimeException("토스페이먼츠 승인 실패: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("토스페이먼츠 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void cancelPayment(Long paymentId, Long memberId, String cancelReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (ID: " + paymentId + ")"));

        if (!Objects.equals(payment.getPurchase().getMemberId(), memberId)) {
            throw new SecurityException("해당 결제에 대한 취소 권한이 없습니다.");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료되지 않은 결제는 취소할 수 없습니다. (현재 상태: " + payment.getStatus() + ")");
        }

        // 토스페이먼츠 API를 통한 결제 취소
        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossPaymentsConfig.getSecretKey() + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 토스페이먼츠 결제 취소 API 요청 본문
        Map<String, String> requestBody = Map.of("cancelReason", cancelReason);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 토스페이먼츠 결제 취소 API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossPaymentsConfig.getApiUrl() + "/" + payment.getPgTid() + "/cancel",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 결제 취소 성공 처리
                payment.setStatus(PaymentStatus.REJECTED);
                paymentRepository.save(payment);

                Purchase purchase = payment.getPurchase();
                purchase.updateStatus(PurchaseStatus.REJECTED);
                purchaseRepository.save(purchase);
            } else {
                // 결제 실패 처리
                throw new RuntimeException("토스페이먼츠 취소 실패: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("토스페이먼츠 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (ID: " + paymentId + ")"));
        return PaymentResponse.fromEntity(payment);
    }

    public Page<PaymentResponse> getPaymentsByMemberId(Long memberId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByPurchase_MemberId(memberId, pageable);
        return payments.map(PaymentResponse::fromEntity);
    }

    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다. (ID: " + paymentId + ")"));
        payment.setStatus(newStatus);
        paymentRepository.save(payment);
    }

    // 테스트를 위해 TossPaymentsConfig와 RestTemplate에 접근할 수 있는 getter 추가
    public TossPaymentsConfig getTossPaymentsConfig() {
        return tossPaymentsConfig;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
