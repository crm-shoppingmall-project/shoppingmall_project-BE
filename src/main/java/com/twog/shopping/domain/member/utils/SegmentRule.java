package com.twog.shopping.domain.member.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentRule {

    private String grade;          // "VIP"
    private String joinDate;       // "last_30_days"
    private String lastPurchase;   // "over_60_days"
    private String agreement;      // "marketing_email"
    private String gender;         // "F" / "M"
    private String ageRange;       // "20-29"
    private String purchaseCategory; // "반려동물"
}
