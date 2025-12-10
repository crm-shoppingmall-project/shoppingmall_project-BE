package com.twog.shopping.domain.support.dto;

import com.twog.shopping.domain.support.entity.TicketChannel;

public record CsTicketRequest(
    TicketChannel csTicketChannel,
    String csTicketCategory,
    String csTicketTitle,
    String csTicketContent
) {
}
