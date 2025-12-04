package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Transactional
    public Long createPurchase(PurchaseRequest request, Long memberId) {

        Purchase purchase = Purchase.builder()
                .memberId(memberId)
                .status(PurchaseStatus.REQUESTED)
                .build();

        request.getItems().forEach(itemDto -> {
            PurchaseDetail detail = PurchaseDetail.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .paidAmount(itemDto.getPrice())
                    .build();

            purchase.addDetail(detail);
        });

        purchaseRepository.save(purchase);
        return purchase.getId();
    }

    public BigDecimal calculateTotalAmount(Long purchaseId) {
        Purchase purchase = findById(purchaseId);

        return purchase.getDetails().stream()
                .map(detail -> detail.getPaidAmount().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Purchase findById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다. (ID: " + purchaseId + ")"));
    }
}