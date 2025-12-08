package com.twog.shopping.domain.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Return_request")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @Column(name = "request_reason", nullable = false, columnDefinition = "TEXT")
    private String requestReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private ReturnRequestStatus requestStatus; // ENUM('REQUESTED','REJECTED','COMPLETED')

    @Column(name = "request_processed")
    private LocalDateTime requestProcessed;

    // ReturnRequestStatus Enum도 필요합니다.
}