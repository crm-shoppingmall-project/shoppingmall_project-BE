package com.twog.shopping.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "history_datetime", nullable = false)
    private LocalDateTime datetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_action_type", nullable = false, length = 50)
    private HistoryActionType historyActionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_action_category", nullable = false, length = 30)
    private HistoryActionCategory actionCategory;

    @Column(name = "history_member_id")
    private Long historyMemberId;

    @Column(name = "history_detail", columnDefinition = "JSON")
    private String historyDetail;

    @Column(name = "history_ip_address", length = 45)
    private String historyIpAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_ref_tbl", length = 50)
    private HistoryRefTable historyRefTbl;

    @Column(name = "history_ref_id")
    private Long historyRefTblId;

    @Column(name = "history_user_agent", length = 512)
    private String userAgent;


    @Builder
    public History(HistoryActionType historyActionType, Long historyMemberId, String historyDetail,
                   String historyIpAddress, HistoryRefTable historyRefTbl) {
        this.historyActionType = historyActionType;
        this.historyMemberId = historyMemberId;
        this.historyDetail = historyDetail;
        this.historyIpAddress = historyIpAddress;
        this.historyRefTbl = historyRefTbl;
    }

    public static History create(
            HistoryActionType actionType,
            Long memberId,
            String detailJson,
            String ipAddress,
            HistoryRefTable refTable,
            Long refId,
            String userAgent
    ) {
        return History.builder()
                .datetime(LocalDateTime.now())
                .historyActionType(actionType)
                .actionCategory(actionType.getCategory())
                .historyMemberId(memberId)
                .historyDetail(detailJson)
                .historyIpAddress(ipAddress)
                .historyRefTbl(refTable)
                .historyRefTblId(refId)
                .userAgent(userAgent)
                .build();
    }
}
