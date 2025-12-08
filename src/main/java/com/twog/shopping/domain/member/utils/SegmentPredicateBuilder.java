package com.twog.shopping.domain.member.utils;

import com.querydsl.core.BooleanBuilder;

public class SegmentPredicateBuilder {

    public BooleanBuilder build(SegmentRule rule){

        QMember member = QMember.member;
        QMemberRfm memberRfm = QMemberRfm.memberRfm;
        QMemberProfile profile = QMemberProfile.memberProfile;
        QPurchase purchase = QPurchase.purchase;
        QProduct product = QProduct.product;

    }
}
