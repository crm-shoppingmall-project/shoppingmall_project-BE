package com.twog.shopping.domain.promotion.dto;

import com.twog.shopping.domain.promotion.entity.Campaign;
import lombok.Getter;

@Getter
public class CampaignDetailResponseDto extends CampaignResponseDto {
    private long totalSentCount;
    private long totalClickedCount;
    private double clickRate;

    public CampaignDetailResponseDto(Campaign campaign, long totalSentCount, long totalClickedCount) {
        super(campaign);
        this.totalSentCount = totalSentCount;
        this.totalClickedCount = totalClickedCount;
        this.clickRate = (totalSentCount > 0) ? (double) totalClickedCount / totalSentCount * 100 : 0.0;
    }
}
