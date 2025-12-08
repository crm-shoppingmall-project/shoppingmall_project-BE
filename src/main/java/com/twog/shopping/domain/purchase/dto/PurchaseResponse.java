package com.twog.shopping.domain.purchase.dto;

<<<<<<< HEAD
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
=======
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import lombok.Builder;
import lombok.Getter;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
=======
import java.math.BigDecimal;
import java.time.LocalDateTime;
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66

@Getter
@Builder
public class PurchaseResponse {
<<<<<<< HEAD
    private Long purchaseId;
    private Long memberId;
    private PurchaseStatus status;
    private List<PurchaseDetailResponse> details;
    private LocalDateTime createdAt;
    private Integer totalAmount; // BigDecimal -> Integer

    @Getter
    @Builder
    public static class PurchaseDetailResponse {
        private Long productId;
        private Integer quantity;
        private Integer paidAmount; // BigDecimal -> Integer

        public static PurchaseDetailResponse fromEntity(PurchaseDetail detail) {
            return PurchaseDetailResponse.builder()
                    .productId(detail.getProductId())
                    .quantity(detail.getQuantity())
                    .paidAmount(detail.getPaidAmount())
                    .build();
        }
    }

    public static PurchaseResponse fromEntity(Purchase purchase) {
        List<PurchaseDetailResponse> detailResponses = purchase.getDetails().stream()
                .map(PurchaseDetailResponse::fromEntity)
                .collect(Collectors.toList());

        Integer totalAmount = detailResponses.stream()
                .map(d -> d.getPaidAmount() * d.getQuantity()) // BigDecimal 계산 -> int 계산
                .reduce(0, Integer::sum); // BigDecimal.ZERO -> 0, BigDecimal::add -> Integer::sum

        return PurchaseResponse.builder()
                .purchaseId(purchase.getId())
                .memberId(purchase.getMemberId())
                .status(purchase.getStatus())
                .details(detailResponses)
                .createdAt(purchase.getCreatedAt())
                .totalAmount(totalAmount)
                .build();
    }
}
=======

    private Long purchaseId;

    private PurchaseStatus status;

    private BigDecimal amount;

    private LocalDateTime purchaseDate;

    private String message;
}
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
