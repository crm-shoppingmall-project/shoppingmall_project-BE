package com.twog.shopping.domain.support.dto;

import java.time.LocalDateTime;

import com.twog.shopping.domain.support.entity.CsTicketReply;

public record CsTicketReplyResponse(
    Long replyId,
    Long replyResponderId,
    String replyContent,
    LocalDateTime replyCreatedAt
) {
    public static CsTicketReplyResponse from(CsTicketReply reply) {
        return new CsTicketReplyResponse(
            reply.getReplyId(),
            reply.getReplyResponderId(),
            reply.getReplyContent(),
            reply.getReplyCreatedAt()
        );
    }
}