package com.twog.shopping.domain.support.dto;

public record CsTicketReplyRequest(
    Long replyResponderId,
    String replyContent
) {
}