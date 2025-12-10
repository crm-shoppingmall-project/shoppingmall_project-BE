package com.twog.shopping.domain.support.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.twog.shopping.domain.support.dto.CsTicketReplyRequest;
import com.twog.shopping.domain.support.dto.CsTicketReplyResponse;
import com.twog.shopping.domain.support.dto.CsTicketRequest;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.service.CsTicketService;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.service.DetailsUser;


@RestController
@RequestMapping("/api/v1/cs-tickets")
public class CsTicketController {

    private final CsTicketService csTicketService;

    public CsTicketController(CsTicketService csTicketService) {
        this.csTicketService = csTicketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CsTicketResponse createTicket(@AuthenticationPrincipal DetailsUser user,
                                         @RequestBody CsTicketRequest req) {
        Long memberId = user.getMember().getMemberId();
        return csTicketService.createTicket(req, memberId);
    }

    @GetMapping
    public Page<CsTicketResponse> getMyTickets(
            @AuthenticationPrincipal DetailsUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "csTicketCreatedAt,desc") String sort) {
        Long memberId = user.getMember().getMemberId();
        return csTicketService.getMyTickets(memberId, page, size, sort);
    }

    @GetMapping("/{id}")
    public CsTicketResponse getTicket(@AuthenticationPrincipal DetailsUser user,
                                      @PathVariable Long id) {
        Long memberId = user.getMember().getMemberId();
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.getTicket(id, memberId, role);
    }

    @PostMapping("/{id}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public CsTicketReplyResponse createReply(@AuthenticationPrincipal DetailsUser user,
                                              @PathVariable Long id, 
                                              @RequestBody CsTicketReplyRequest req) {
        Long responderId = user.getMember().getMemberId();
        return csTicketService.createReply(id, responderId, req);
    }
}