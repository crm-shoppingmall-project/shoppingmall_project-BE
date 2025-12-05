package com.twog.shopping.global.common.entity;


public enum GradeName {
    BRONZE(2),
    SILVER(3),
    GOLD(4),
    VIP(10);

    private final int discountRate;

    GradeName(int discountRate) {
        this.discountRate = discountRate;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public int applyDiscountRate(int productPrice){
        return productPrice * (100 - discountRate) / 100;
    }


//    int rate = member.getMemberGrade().getGradeName().getDiscountRate();

}
