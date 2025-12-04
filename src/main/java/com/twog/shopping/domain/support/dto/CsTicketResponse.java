package com.twog.shopping.domain.support.dto;

import java.time.LocalDateTime;

import com.twog.shopping.domain.support.entity.CsTicket;
import com.twog.shopping.domain.support.entity.TicketChannel;
import com.twog.shopping.domain.support.entity.TicketStatus;

public record CsTicketResponse(
    Long csTicketId,
    Long memberId,
    TicketChannel csTicketChannel,
    String csTicketCategory,
    TicketStatus csTicketStatus,
    String csTicketTitle,
    String csTicketContent,
    LocalDateTime csTicketCreatedAt
) {
    // Entity -> Response DTO 변환 팩토리 메서드
    public static CsTicketResponse from(CsTicket ticket) {
        return new CsTicketResponse(
            ticket.getCsTicketId(),
            ticket.getMember().getMemberId(),
            ticket.getCsTicketChannel(),
            ticket.getCsTicketCategory(),
            ticket.getCsTicketStatus(),
            ticket.getCsTicketTitle(),
            ticket.getCsTicketContent(),
            ticket.getCsTicketCreatedAt()
        );
    }
}
