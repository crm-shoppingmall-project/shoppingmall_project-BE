package com.twog.shopping.domain.support.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CsTicketResponse createTicket(@AuthenticationPrincipal DetailsUser user,
                                         @ModelAttribute CsTicketRequest req) {
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

    // 관리자용: 전체 티켓 조회
    @GetMapping("/admin/all")
    public Page<CsTicketResponse> getAllTickets(
            @AuthenticationPrincipal DetailsUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "csTicketCreatedAt,desc") String sort) {
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.getAllTickets(role, page, size, sort);
    }

    @GetMapping("/{id}")
    public CsTicketResponse getTicket(@AuthenticationPrincipal DetailsUser user,
                                      @PathVariable Long id) {
        Long memberId = user.getMember().getMemberId();
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.getTicket(id, memberId, role);
    }

    // 티켓의 답변 목록 조회
    @GetMapping("/{id}/replies")
    public java.util.List<CsTicketReplyResponse> getReplies(
            @AuthenticationPrincipal DetailsUser user,
            @PathVariable Long id) {
        Long memberId = user.getMember().getMemberId();
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.getReplies(id, memberId, role);
    }

    @PostMapping(value = "/{id}/replies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CsTicketReplyResponse createReply(@AuthenticationPrincipal DetailsUser user,
                                              @PathVariable Long id, 
                                              @ModelAttribute CsTicketReplyRequest req) {
        Long responderId = user.getMember().getMemberId();
        return csTicketService.createReply(id, responderId, req);
    }

    // 관리자용: 티켓 상태 변경
    @PostMapping("/{id}/status")
    public CsTicketResponse changeStatus(
            @AuthenticationPrincipal DetailsUser user,
            @PathVariable Long id,
            @RequestParam String status) {
        Long memberId = user.getMember().getMemberId();
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.changeTicketStatus(id, memberId, role, status);
    }
}