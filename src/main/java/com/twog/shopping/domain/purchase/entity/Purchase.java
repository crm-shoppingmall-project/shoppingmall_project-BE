package com.twog.shopping.domain.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Purchase")
public class Purchase {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_status")
    private PurchaseStatus status;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseDetail> details = new ArrayList<>();

    @Column(name = "purchase_processed")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addDetail(PurchaseDetail detail) {
        this.details.add(detail);
        if (detail.getPurchase() != this) {
            detail.setPurchase(this);
        }
    }

    public void updateStatus(PurchaseStatus newStatus) {
        this.status = newStatus;
        if (newStatus == PurchaseStatus.COMPLETED) {
            this.processedAt = LocalDateTime.now();
        }
    }
}