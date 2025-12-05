package com.twog.shopping.domain.purchase.dto;

import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PurchaseResponse {

    private Long purchaseId;

    private PurchaseStatus status;

    private BigDecimal amount;

    private LocalDateTime purchaseDate;

    private String message;
}
