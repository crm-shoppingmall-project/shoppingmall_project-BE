package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.global.error.exception.OutOfStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long createPurchase(PurchaseRequest request, Long memberId) {

        Purchase purchase = Purchase.builder()
                .memberId(memberId)
                .status(PurchaseStatus.REQUESTED)
                .build();

        for (PurchaseRequest.PurchaseItemDto itemDto : request.getItems()) {

            Product product = productRepository.findById(itemDto.getProductId().intValue())
                    .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다. (ID: " + itemDto.getProductId() + ")"));

            if (!product.isStock(itemDto.getQuantity())) {
                throw new OutOfStockException("상품의 재고가 부족합니다. (ID: " + product.getProductId() + ")");
            }
            product.decreaseStock(itemDto.getQuantity());

            int itemActualPrice = product.getProductPrice(); // BigDecimal -> int

            PurchaseDetail detail = PurchaseDetail.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .paidAmount(itemActualPrice)
                    .build();

            purchase.addDetail(detail);
        }

        purchaseRepository.save(purchase);

        Integer serverCalculatedTotal = calculateTotalAmount(purchase.getId()); // BigDecimal -> Integer

        if (!Objects.equals(serverCalculatedTotal, request.getTotalAmount())) { // compareTo -> Objects.equals
            throw new IllegalStateException("총 결제 금액 불일치. 위변조가 의심됩니다. (서버 계산: " + serverCalculatedTotal + ", 요청: " + request.getTotalAmount() + ")");
        }

        return purchase.getId();
    }

    public Page<PurchaseResponse> findMyPurchases(Long memberId, Pageable pageable) {
        Page<Purchase> purchases = purchaseRepository.findByMemberId(memberId, pageable);
        return purchases.map(PurchaseResponse::fromEntity);
    }

    @Transactional
    public void cancelPurchase(Long purchaseId, Long memberId) {
        Purchase purchase = findAndValidateOwner(purchaseId, memberId);

        if (purchase.getStatus() != PurchaseStatus.REQUESTED) {
            throw new IllegalStateException("주문을 취소할 수 없는 상태입니다.");
        }

        purchase.updateStatus(PurchaseStatus.REJECTED);

        for (PurchaseDetail detail : purchase.getDetails()) {
            Product product = productRepository.findById(detail.getProductId().intValue())
                    .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다. (ID: " + detail.getProductId() + ")"));
            product.decreaseStock(-detail.getQuantity());
        }
    }

    @Transactional
    public void requestReturn(Long purchaseId, Long memberId) {
        Purchase purchase = findAndValidateOwner(purchaseId, memberId);

        if (purchase.getStatus() != PurchaseStatus.COMPLETED) {
            throw new IllegalStateException("반품/교환을 요청할 수 없는 상태입니다.");
        }
        purchase.updateStatus(PurchaseStatus.REJECTED); // RETURN_REQUESTED 대신 REJECTED로 임시 변경
    }

    public Integer calculateTotalAmount(Long purchaseId) { // BigDecimal -> Integer
        Purchase purchase = findById(purchaseId);

        return purchase.getDetails().stream()
                .mapToInt(detail -> detail.getPaidAmount() * detail.getQuantity()) // BigDecimal -> int
                .sum(); // reduce(BigDecimal.ZERO, BigDecimal::add) -> sum()
    }

    public Purchase findById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다. (ID: " + purchaseId + ")"));
    }

    private Purchase findAndValidateOwner(Long purchaseId, Long memberId) {
        Purchase purchase = findById(purchaseId);
        if (!Objects.equals(purchase.getMemberId(), memberId)) {
            throw new SecurityException("주문에 대한 권한이 없습니다.");
        }
        return purchase;
    }
}