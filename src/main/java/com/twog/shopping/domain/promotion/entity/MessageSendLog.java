package com.twog.shopping.domain.promotion.entity;

import com.twog.shopping.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "Message_send_log")
public class MessageSendLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_id")
    private Long sendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "send_at", nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime sendAt;

    @Column(name = "send_clicked")
    private LocalDateTime sendClicked;

    @Builder
    public MessageSendLog(Campaign campaign, Member member) {
        this.campaign = campaign;
        this.member = member;
    }

    public void markAsClicked() {
        this.sendClicked = LocalDateTime.now();
    }
}
