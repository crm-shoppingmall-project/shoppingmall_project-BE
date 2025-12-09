package com.twog.shopping.domain.promotion.scheduler;

import com.twog.shopping.domain.promotion.entity.Campaign;
import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import com.twog.shopping.domain.promotion.repository.CampaignRepository;
import com.twog.shopping.domain.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignBatchScheduler {

    private final CampaignRepository campaignRepository;
    private final PromotionService promotionService;

    // 매일 자정(00:00:00)에 실행되는 배치 - RUNNING 상태이고 예약 시간이 된 캠페인 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void executeScheduledCampaigns() {
        List<Campaign> campaigns = campaignRepository.findAllByCampaignStatusAndCampaignScheduledBefore(
                CampaignStatus.RUNNING, LocalDateTime.now());

        for (Campaign campaign : campaigns) {
            try {
                promotionService.executeCampaign(campaign.getCampaignId());
            } catch (Exception e) {
                // 개별 캠페인 실패 시 로깅, 전체 배치 중단 방지
                log.error("Campaign execution failed: " + campaign.getCampaignId(), e);
            }
        }
    }
}
