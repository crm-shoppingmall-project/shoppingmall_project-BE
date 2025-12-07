package com.twog.shopping.domain.log.entity;

import lombok.Getter;

@Getter
public enum HistoryActionType {

    VIEW_PRODUCT(HistoryActionCategory.VIEW),
    VIEW_CATEGORY(HistoryActionCategory.VIEW),
    VIEW_CART(HistoryActionCategory.VIEW),
    VIEW_HOME(HistoryActionCategory.VIEW),


    ADD_CART(HistoryActionCategory.ENGAGE),
    REMOVE_CART_ITEM(HistoryActionCategory.ENGAGE),
    ADD_WISHLIST(HistoryActionCategory.ENGAGE),
    CLICK_BANNER(HistoryActionCategory.ENGAGE),


    PURCHASE_REQUEST(HistoryActionCategory.PURCHASE),
    PURCHASE_COMPLETED(HistoryActionCategory.PURCHASE),
    PURCHASE_CANCELLED(HistoryActionCategory.PURCHASE),

    REFUND_REQUEST(HistoryActionCategory.PURCHASE),
    REFUND_COMPLETED(HistoryActionCategory.PURCHASE),


    LOGIN_SUCCESS(HistoryActionCategory.AUTH),
    LOGIN_FAIL(HistoryActionCategory.AUTH),
    SIGNUP_SUCCESS(HistoryActionCategory.AUTH),
    LOGOUT(HistoryActionCategory.AUTH),


    CS_TICKET_CREATED(HistoryActionCategory.CS),
    CS_TICKET_REPLIED(HistoryActionCategory.CS),
    CS_TICKET_CLOSED(HistoryActionCategory.CS),


    SYSTEM_UPDATE(HistoryActionCategory.SYSTEM);

    private final HistoryActionCategory category;

    HistoryActionType(HistoryActionCategory category) {
        this.category = category;
    }

}