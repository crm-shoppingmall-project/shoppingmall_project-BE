package com.twog.shopping.domain.promotion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "History")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @CreationTimestamp
    @Column(name = "history_datetime", nullable = false, updatable = false)
    private LocalDateTime historyDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_action_type", nullable = false)
    private HistoryActionType historyActionType;

    @Column(name = "history_member_id", nullable = false)
    private Long historyMemberId;

    @Column(name = "history_detail", columnDefinition = "TEXT")
    private String historyDetail;

    @Column(name = "history_ip_address", nullable = false)
    private String historyIpAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_ref_tbl", nullable = false)
    private HistoryRefTable historyRefTbl;

    @Builder
    public History(HistoryActionType historyActionType, Long historyMemberId, String historyDetail,
            String historyIpAddress, HistoryRefTable historyRefTbl) {
        this.historyActionType = historyActionType;
        this.historyMemberId = historyMemberId;
        this.historyDetail = historyDetail;
        this.historyIpAddress = historyIpAddress;
        this.historyRefTbl = historyRefTbl;
    }

    public enum HistoryActionType {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, PAID, REFUND
    }

    public enum HistoryRefTable {
        Member, Purchase, Segment, CS_ticket, Product, Return_request, CART_ITEM
    }
}
