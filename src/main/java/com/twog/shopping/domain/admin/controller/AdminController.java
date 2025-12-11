package com.twog.shopping.domain.admin.controller;

import com.twog.shopping.domain.log.dto.HistoryResponseDTO;
import com.twog.shopping.domain.log.service.HistoryService;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.domain.support.repository.CsTicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin API", description = "관리자 대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final CsTicketRepository csTicketRepository;
    private final HistoryService historyService;

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

    @Operation(summary = "회원 로그 조회", description = "관리자가 전체 회원 로그를 조회합니다.")
    @GetMapping("/logs")
    public ResponseEntity<List<HistoryResponseDTO>> getAllLogs() {
        try {
            System.out.println("=== getAllLogs 시작 ===");
            List<HistoryResponseDTO> logs = historyService.getAllHistories();
            System.out.println("=== 로그 개수: " + logs.size() + " ===");
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            System.out.println("=== getAllLogs 에러 ===");
            e.printStackTrace();
            throw e;
        }
    }

    @Operation(summary = "회원 로그 페이징 조회", description = "관리자가 전체 회원 로그를 페이징으로 조회합니다.")
    @GetMapping("/logs/page")
    public ResponseEntity<Page<HistoryResponseDTO>> getAllLogsPage(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(historyService.getAllHistoriesPage(pageable));
    }
}
