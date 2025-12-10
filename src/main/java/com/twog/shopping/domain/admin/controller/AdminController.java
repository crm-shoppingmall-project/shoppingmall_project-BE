package com.twog.shopping.domain.admin.controller;

import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.domain.support.repository.CsTicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Admin API", description = "관리자 대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final CsTicketRepository csTicketRepository;

    @Operation(summary = "대시보드 통계", description = "관리자 대시보드용 통계 데이터를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        long productCount = productRepository.count();
        long orderCount = purchaseRepository.count();
        long ticketCount = csTicketRepository.count();

        return ResponseEntity.ok(Map.of(
                "products", productCount,
                "orders", orderCount,
                "tickets", ticketCount
        ));
    }
}
