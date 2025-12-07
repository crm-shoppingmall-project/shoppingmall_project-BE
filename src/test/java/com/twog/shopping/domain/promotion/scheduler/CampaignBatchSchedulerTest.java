package com.twog.shopping.domain.promotion.scheduler;

import com.twog.shopping.domain.promotion.entity.Campaign;
import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import com.twog.shopping.domain.promotion.repository.CampaignRepository;
import com.twog.shopping.domain.promotion.service.PromotionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignBatchSchedulerTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private CampaignBatchScheduler scheduler;

    @Test
    @DisplayName("예약된 캠페인이 없을 경우 아무 작업도 하지 않는다")
    void executeScheduledCampaigns_NoCampaigns() {
        // given
        when(campaignRepository.findAllByCampaignStatusAndCampaignScheduledBefore(
                eq(CampaignStatus.RUNNING), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // when
        scheduler.executeScheduledCampaigns();

        // then
        verify(promotionService, never()).executeCampaign(any());
    }

    @Test
    @DisplayName("예약된 캠페인을 순차적으로 실행한다")
    void executeScheduledCampaigns_Success() {
        // given
        Campaign campaign1 = Campaign.builder().campaignStatus(CampaignStatus.RUNNING).build();
        ReflectionTestUtils.setField(campaign1, "campaignId", 1L);

        Campaign campaign2 = Campaign.builder().campaignStatus(CampaignStatus.RUNNING).build();
        ReflectionTestUtils.setField(campaign2, "campaignId", 2L);

        when(campaignRepository.findAllByCampaignStatusAndCampaignScheduledBefore(
                eq(CampaignStatus.RUNNING), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(campaign1, campaign2));

        // when
        scheduler.executeScheduledCampaigns();

        // then
        verify(promotionService).executeCampaign(1L);
        verify(promotionService).executeCampaign(2L);
    }

    @Test
    @DisplayName("중간에 에러가 발생해도 다음 캠페인은 계속 실행되어야 한다")
    void executeScheduledCampaigns_ContinueOnError() {
        // given
        Campaign campaign1 = Campaign.builder().campaignStatus(CampaignStatus.RUNNING).build();
        ReflectionTestUtils.setField(campaign1, "campaignId", 1L);

        Campaign campaign2 = Campaign.builder().campaignStatus(CampaignStatus.RUNNING).build();
        ReflectionTestUtils.setField(campaign2, "campaignId", 2L);

        when(campaignRepository.findAllByCampaignStatusAndCampaignScheduledBefore(
                eq(CampaignStatus.RUNNING), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(campaign1, campaign2));

        // 첫 번째 캠페인 실행 시 예외 발생
        doThrow(new RuntimeException("Error")).when(promotionService).executeCampaign(1L);

        // when
        scheduler.executeScheduledCampaigns();

        // then
        verify(promotionService).executeCampaign(1L); // 호출은 되었음
        verify(promotionService).executeCampaign(2L); // 1번 실패 후에도 호출되어야 함
    }
}
