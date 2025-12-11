package com.twog.shopping.domain.log.entity;

import lombok.Getter;

@Getter
public enum HistoryActionType {

    // VIEW
    VIEW_PRODUCT(HistoryActionCategory.VIEW),
    VIEW_CATEGORY(HistoryActionCategory.VIEW),
    VIEW_CART(HistoryActionCategory.VIEW),
    VIEW_HOME(HistoryActionCategory.VIEW),
    PAGE_VIEW(HistoryActionCategory.VIEW),

    // ENGAGE
    ADD_CART(HistoryActionCategory.ENGAGE),
    ADD_TO_CART(HistoryActionCategory.ENGAGE),
    REMOVE_CART_ITEM(HistoryActionCategory.ENGAGE),
    REMOVE_FROM_CART(HistoryActionCategory.ENGAGE),
    ADD_WISHLIST(HistoryActionCategory.ENGAGE),
    CLICK_BANNER(HistoryActionCategory.ENGAGE),
    CART_ADD(HistoryActionCategory.ENGAGE),
    CART_REMOVE(HistoryActionCategory.ENGAGE),

    // PURCHASE
    PURCHASE_REQUEST(HistoryActionCategory.PURCHASE),
    PURCHASE_COMPLETED(HistoryActionCategory.PURCHASE),
    PURCHASE_CANCELLED(HistoryActionCategory.PURCHASE),
    ORDER_PLACED(HistoryActionCategory.PURCHASE),
    ORDER_COMPLETED(HistoryActionCategory.PURCHASE),
    ORDER_CANCELLED(HistoryActionCategory.PURCHASE),
    PAYMENT_SUCCESS(HistoryActionCategory.PURCHASE),
    PAYMENT_FAILED(HistoryActionCategory.PURCHASE),
    PAYMENT_CANCELLED(HistoryActionCategory.PURCHASE),
    REFUND_REQUEST(HistoryActionCategory.PURCHASE),
    REFUND_COMPLETED(HistoryActionCategory.PURCHASE),
    REFUND_COMPLETE(HistoryActionCategory.PURCHASE),

    // AUTH
    LOGIN(HistoryActionCategory.AUTH),
    LOGIN_SUCCESS(HistoryActionCategory.AUTH),
    LOGIN_FAIL(HistoryActionCategory.AUTH),
    SIGNUP(HistoryActionCategory.AUTH),
    SIGNUP_SUCCESS(HistoryActionCategory.AUTH),
    LOGOUT(HistoryActionCategory.AUTH),

    // CS
    CS_TICKET_CREATED(HistoryActionCategory.CS),
    CS_TICKET_REPLIED(HistoryActionCategory.CS),
    CS_TICKET_CLOSED(HistoryActionCategory.CS),

    // SYSTEM
    SYSTEM_UPDATE(HistoryActionCategory.SYSTEM);

    private final HistoryActionCategory category;

    HistoryActionType(HistoryActionCategory category) {
        this.category = category;
    }

}