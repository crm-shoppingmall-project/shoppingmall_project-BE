package com.twog.shopping.domain.support.controller;

import java.util.List;

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


import com.twog.shopping.domain.support.dto.CsTicketRequest;
import com.twog.shopping.domain.support.dto.CsTicketReplyResponse;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.service.CsTicketService;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.service.DetailsUser;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cs-tickets")
@Tag(name = "CS Ticket", description = "고객센터 문의 API (사용자용)")
public class CsTicketController {

    private final CsTicketService csTicketService;

    public CsTicketController(CsTicketService csTicketService) {
        this.csTicketService = csTicketService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "문의 등록", description = "새로운 1:1 문의를 등록합니다.")
    public CsTicketResponse createTicket(@AuthenticationPrincipal DetailsUser user,
                                         @ModelAttribute CsTicketRequest req) {
        Long memberId = user.getMember().getMemberId();
        return csTicketService.createTicket(req, memberId);
    }

    @GetMapping
    @Operation(summary = "내 문의 목록 조회", description = "자신이 작성한 문의 내역을 페이징하여 조회합니다.")
    public Page<CsTicketResponse> getMyTickets(
            @AuthenticationPrincipal DetailsUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "csTicketCreatedAt,desc") String sort) {
        Long memberId = user.getMember().getMemberId();
        return csTicketService.getMyTickets(memberId, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "문의 상세 조회", description = "특정 문의 내역의 상세 정보를 조회합니다.")
    public CsTicketResponse getTicket(@AuthenticationPrincipal DetailsUser user,
                                      @PathVariable Long id) {
        Long memberId = user.getMember().getMemberId();
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.getTicket(id, memberId, role);
    }

    @GetMapping("/{id}/replies")
    @Operation(summary = "문의 답변 목록 조회", description = "특정 문의에 대한 답변 목록을 조회합니다.")
    public List<CsTicketReplyResponse> getReplies(@AuthenticationPrincipal DetailsUser user,
                                                   @PathVariable Long id) {
        Long memberId = user.getMember().getMemberId();
        UserRole role = user.getMember().getMemberRole();
        return csTicketService.getReplies(id, memberId, role);
    }
}