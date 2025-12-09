package com.twog.shopping.domain.analytics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RfmAggregation {

    Long getMemberId();
    LocalDateTime getLastPurchaseAt();
    Long getFrequency();
    BigDecimal getMonetary();

}
