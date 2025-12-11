package com.twog.shopping.domain.support.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.support.dto.CsTicketReplyRequest;
import com.twog.shopping.domain.support.dto.CsTicketReplyResponse;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.service.CsTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/cs-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin CS Ticket", description = "고객센터 문의 관리 API (관리자용)")
public class AdminCsTicketController {

    private final CsTicketService csTicketService;

    // 관리자용 - 모든 문의 내역 조회 (답변을 달기 위해 필요)
    @GetMapping
    @Operation(summary = "전체 문의 목록 조회", description = "모든 사용자의 문의 내역을 페이징하여 조회합니다.")
    public Page<CsTicketResponse> getAllTickets(
            @AuthenticationPrincipal DetailsUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "csTicketCreatedAt,desc") String sort) {
        return csTicketService.getAllTickets(user.getMember().getMemberRole(), page, size, sort);
    }

    // 관리자용 - 답변 등록
    @PostMapping(value = "/{id}/replies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "답변 등록", description = "특정 문의에 대한 관리자 답변을 등록합니다.")
    public CsTicketReplyResponse createReply(@AuthenticationPrincipal DetailsUser user,
                                             @PathVariable Long id,
                                             @ModelAttribute CsTicketReplyRequest req) {
        Long responderId = user.getMember().getMemberId();
        return csTicketService.createReply(id, responderId, req);
    }

    // 관리자용 - 티켓 상태 변경
    @PostMapping("/{id}/status")
    @Operation(summary = "티켓 상태 변경", description = "특정 문의의 상태를 변경합니다.")
    public CsTicketResponse changeStatus(@AuthenticationPrincipal DetailsUser user,
                                         @PathVariable Long id,
                                         @RequestParam String status) {
        Long memberId = user.getMember().getMemberId();
        return csTicketService.changeTicketStatus(id, memberId, user.getMember().getMemberRole(), status);
    }
}
