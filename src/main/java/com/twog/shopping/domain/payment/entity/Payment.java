package com.twog.shopping.domain.payment.entity;

import com.twog.shopping.domain.purchase.entity.Purchase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @Column(name = "payment_tid", nullable = false, unique = true, length = 255) // 20 -> 255로 변경
    private String pgTid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType type;

    @Column(name = "payment_paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_created", nullable = false, updatable = false)
    private LocalDateTime paymentCreated;

    @PrePersist
    protected void onCreate() {
        this.paymentCreated = LocalDateTime.now();
    }

    public void setStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }

    public void updatePgTid(String pgTid) {
        this.pgTid = pgTid;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
