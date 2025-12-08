package com.twog.shopping.domain.log.entity;

import com.twog.shopping.domain.log.entity.HistoryActionCategory;
import com.twog.shopping.domain.log.entity.HistoryActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

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
    private Long id;

    @Column(name = "history_datetime", nullable = false)
    private LocalDateTime datetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_action_type", nullable = false, length = 50)
    private HistoryActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_action_category", nullable = false, length = 30)
    private HistoryActionCategory actionCategory;

    @Column(name = "history_member_id")
    private Long memberId;

    @Column(name = "history_detail", columnDefinition = "JSON")
    private String detailJson;

    @Column(name = "history_ip_address", length = 45)
    private String ipAddress;

    @Column(name = "history_ref_tbl", length = 50)
    private String refTable;

    @Column(name = "history_ref_id")
    private Long refId;

    @Column(name = "history_user_agent", length = 512)
    private String userAgent;

    public static History create(
            HistoryActionType actionType,
            Long memberId,
            String detailJson,
            String ipAddress,
            String refTable,
            Long refId,
            String userAgent
    ) {
        return History.builder()
                .datetime(LocalDateTime.now())
                .actionType(actionType)
                .actionCategory(actionType.getCategory())
                .memberId(memberId)
                .detailJson(detailJson)
                .ipAddress(ipAddress)
                .refTable(refTable)
                .refId(refId)
                .userAgent(userAgent)
                .build();
    }
}
