package com.twog.shopping.domain.analytics.entity;

import com.twog.shopping.global.common.entity.GradeName;

public interface MemberGradeTarget {

    Long getMemberId();
    int getRfmTotalScore();
    GradeName getCurrentGrade();

}
