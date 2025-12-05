package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.product.service.ProductService;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseDetailRepository;
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
    private final PurchaseDetailRepository purchaseDetailRepository;
    // private final ProductService productService;

    public Purchase findById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NoSuchElementException("주문 ID: " + purchaseId + "을 찾을 수 없습니다."));
    }

    public BigDecimal calculateTotalAmount(Long purchaseId) {
        Purchase purchase = findById(purchaseId);

        BigDecimal total = purchase.getDetails().stream()
                .map(detail -> detail.getPaidAmount().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("주문 ID: " + purchaseId + "의 총 금액이 0원 이하 입니다.");
        }
        return total;
    }
}