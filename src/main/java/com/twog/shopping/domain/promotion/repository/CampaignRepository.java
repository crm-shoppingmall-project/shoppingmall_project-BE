package com.twog.shopping.domain.promotion.repository;

import com.twog.shopping.domain.promotion.entity.Campaign;
import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findAllByCampaignStatusAndCampaignScheduledBefore(CampaignStatus status, LocalDateTime time);
}
