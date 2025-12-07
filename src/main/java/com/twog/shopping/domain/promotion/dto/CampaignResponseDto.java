package com.twog.shopping.domain.promotion.dto;

import com.twog.shopping.domain.promotion.entity.Campaign;
import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CampaignResponseDto {
    private Long campaignId;
    private String campaignName;
    private CampaignStatus campaignStatus;
    private LocalDateTime campaignScheduled;
    private String campaignContent;

    public CampaignResponseDto(Campaign campaign) {
        this.campaignId = campaign.getCampaignId();
        this.campaignName = campaign.getCampaignName();
        this.campaignStatus = campaign.getCampaignStatus();
        this.campaignScheduled = campaign.getCampaignScheduled();
        this.campaignContent = campaign.getCampaignContent();
    }
}
