package com.twog.shopping.domain.payment.service;

import com.twog.shopping.domain.payment.dto.PaymentRequest;
import com.twog.shopping.domain.payment.dto.PaymentResponse;
import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.entity.PaymentType;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.global.config.TossPaymentsConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

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

        if (paymentRepository.findByPurchase_Id(purchase.getId()).stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getType() == PaymentType.PAYMENT)) {
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
                .pgTid(UUID.randomUUID().toString().substring(0, 20))
                .status(PaymentStatus.REQUESTED)
                .type(request.getPaymentType())
                .paidAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return savedPayment.getId();
    }

    @Transactional
    public void confirmTossPayment(String paymentKey, String orderId, Integer amount) {
        log.info("Toss Payments Secret Key: {}", tossPaymentsConfig.getSecretKey());
        log.info("Toss Payments API URL: {}", tossPaymentsConfig.getApiUrl());

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

        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossPaymentsConfig.getApiUrl() + "/confirm",
                    requestEntity,
                    Map.class
            );

            log.info("Toss API Response Status: {}", response.getStatusCode());
            log.info("Toss API Response Body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("결제 승인 성공! DB 상태를 COMPLETED로 변경합니다.");
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.updatePgTid(paymentKey);
                paymentRepository.save(payment);

                purchase.updateStatus(PurchaseStatus.COMPLETED);
                purchaseRepository.save(purchase);
            } else {
                log.error("결제 승인 실패! Toss API가 OK가 아닌 상태를 반환했습니다.");
                throw new RuntimeException("토스페이먼츠 승인 실패: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Toss API 호출 중 예외 발생", e);
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

        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossPaymentsConfig.getSecretKey() + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("cancelReason", cancelReason);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossPaymentsConfig.getApiUrl() + "/" + payment.getPgTid() + "/cancel",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Payment refundPayment = Payment.builder()
                        .purchase(payment.getPurchase())
                        .pgTid(payment.getPgTid())
                        .status(PaymentStatus.REJECTED)
                        .type(PaymentType.REFUND)
                        .paidAt(LocalDateTime.now())
                        .build();
                paymentRepository.save(refundPayment);

                Purchase purchase = payment.getPurchase();
                purchase.updateStatus(PurchaseStatus.REJECTED);
                purchaseRepository.save(purchase);
            } else {
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
}
