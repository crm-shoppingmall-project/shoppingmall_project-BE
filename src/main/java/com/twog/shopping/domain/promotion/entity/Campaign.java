package com.twog.shopping.domain.promotion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import lombok.Builder; // Added import
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status", nullable = false)
    private CampaignStatus campaignStatus;

    @Column(name = "campaign_scheduled", nullable = false)
    private LocalDateTime campaignScheduled;

    @Column(name = "campaign_content", nullable = false, columnDefinition = "TEXT")
    private String campaignContent;

    @Builder
    public Campaign(String campaignName, CampaignStatus campaignStatus, LocalDateTime campaignScheduled,
            String campaignContent) {
        this.campaignName = campaignName;
        this.campaignStatus = campaignStatus;
        this.campaignScheduled = campaignScheduled;
        this.campaignContent = campaignContent;
    }

    public void updateInfo(String campaignName, String campaignContent, LocalDateTime campaignScheduled) {
        this.campaignName = campaignName;
        this.campaignContent = campaignContent;
        this.campaignScheduled = campaignScheduled;
    }

    public void complete() {
        this.campaignStatus = CampaignStatus.COMPLETED;
    }

    public void deleteLogical() {
        this.campaignStatus = CampaignStatus.ENDED;
    }

    public void end() {
        this.campaignStatus = CampaignStatus.ENDED;
    }
}
