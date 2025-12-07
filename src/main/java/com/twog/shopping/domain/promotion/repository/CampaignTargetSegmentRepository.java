package com.twog.shopping.domain.promotion.repository;

import com.twog.shopping.domain.promotion.entity.CampaignTargetSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignTargetSegmentRepository extends JpaRepository<CampaignTargetSegment, Long> {
    List<CampaignTargetSegment> findByCampaign_CampaignId(Long campaignId);

    void deleteByCampaign_CampaignId(Long campaignId);
}
