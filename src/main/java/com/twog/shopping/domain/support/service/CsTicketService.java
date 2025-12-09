package com.twog.shopping.domain.support.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.support.dto.CsTicketReplyRequest;
import com.twog.shopping.domain.support.dto.CsTicketReplyResponse;
import com.twog.shopping.domain.support.dto.CsTicketRequest;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.entity.CsTicket;
import com.twog.shopping.domain.support.entity.CsTicketReply;
import com.twog.shopping.domain.support.repository.CsTicketReplyRepository;
import com.twog.shopping.domain.support.repository.CsTicketRepository;

@Service
public class CsTicketService {

  private final CsTicketRepository csTicketRepository;
  private final CsTicketReplyRepository csTicketReplyRepository;
  private final MemberRepository memberRepository;

  public CsTicketService(CsTicketRepository csTicketRepository, CsTicketReplyRepository csTicketReplyRepository, MemberRepository memberRepository) {
    this.csTicketRepository = csTicketRepository;
    this.csTicketReplyRepository = csTicketReplyRepository;
    this.memberRepository = memberRepository;
  }

  @Transactional
  public CsTicketResponse createTicket(CsTicketRequest req, Long authenticatedMemberId) {
    // 인증된 사용자 기반으로만 티켓 생성. 요청 DTO의 memberId는 무시한다.
    if (authenticatedMemberId == null || authenticatedMemberId <= 0) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }

    Member member = memberRepository.findById(authenticatedMemberId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

    CsTicket ticket = CsTicket.create(req, member);
    csTicketRepository.save(ticket);

    return CsTicketResponse.from(ticket);
  }

  @Transactional(readOnly = true)
  public Page<CsTicketResponse> getMyTickets(Long memberId, Pageable pageable) {
    Page<CsTicket> tickets = csTicketRepository.findByMember_MemberId(memberId, pageable);
    return tickets.map(CsTicketResponse::from);
  }

  @Transactional(readOnly = true)
  public CsTicketResponse getTicket(Long csTicketId) {
    CsTicket ticket = csTicketRepository.findById(csTicketId)
        .orElseThrow(() -> new RuntimeException("문의 내역을 찾을 수 없습니다."));
    return CsTicketResponse.from(ticket);
  }

  @Transactional
  public CsTicketReplyResponse createReply(Long csTicketId, CsTicketReplyRequest req) {
    CsTicket ticket = csTicketRepository.findById(csTicketId)
        .orElseThrow(() -> new RuntimeException("문의 내역을 찾을 수 없습니다."));
    
    CsTicketReply reply = CsTicketReply.create(ticket, req.replyResponderId(), req.replyContent());
    csTicketReplyRepository.save(reply);
    
    return CsTicketReplyResponse.from(reply);
  }
}
