package com.twog.shopping.domain.promotion.repository;

import com.twog.shopping.domain.promotion.entity.MessageSendLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MessageSendLogRepository extends JpaRepository<MessageSendLog, Long> {
    long countByCampaign_CampaignId(Long campaignId);

    long countByCampaign_CampaignIdAndSendClickedIsNotNull(Long campaignId);

    Page<MessageSendLog> findByCampaign_CampaignId(Long campaignId, Pageable pageable);

    void deleteByCampaign_CampaignId(Long campaignId);
}
