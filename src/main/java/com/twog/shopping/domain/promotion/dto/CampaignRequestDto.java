package com.twog.shopping.domain.promotion.dto;

import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CampaignRequestDto {
    private String campaignName;
    private String campaignContent;
    private LocalDateTime campaignScheduled;
    private List<Long> targetSegmentIds;

    @Builder
    public CampaignRequestDto(String campaignName, String campaignContent, LocalDateTime campaignScheduled,
            List<Long> targetSegmentIds) {
        this.campaignName = campaignName;
        this.campaignContent = campaignContent;
        this.campaignScheduled = campaignScheduled;
        this.targetSegmentIds = targetSegmentIds;
    }
}
