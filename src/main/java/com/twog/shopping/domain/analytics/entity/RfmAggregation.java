package com.twog.shopping.domain.analytics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RfmAggregation {

    Long getMemberId();
    LocalDateTime getLastPurchasseAt();
    Long getFrequency();
    BigDecimal getMonetary();

}
