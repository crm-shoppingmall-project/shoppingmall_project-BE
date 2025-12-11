package com.twog.shopping.domain.support.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.UserRole;
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
  public Page<CsTicketResponse> getMyTickets(Long memberId, int page, int size, String sort) {
    String[] sortParams = sort.split(",");
    String sortField = sortParams[0];
    Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
        ? Sort.Direction.ASC 
        : Sort.Direction.DESC;
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
    Page<CsTicket> tickets = csTicketRepository.findByMember_MemberId(memberId, pageable);
    return tickets.map(CsTicketResponse::from);
  }



  @Transactional(readOnly = true)
  public CsTicketResponse getTicket(Long csTicketId, Long memberId, UserRole role) {
    CsTicket ticket = csTicketRepository.findById(csTicketId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 내역을 찾을 수 없습니다."));
    
    // 본인의 티켓이거나 ADMIN인 경우에만 조회 가능
    boolean isOwner = ticket.getMember().getMemberId().equals(memberId);
    boolean isAdmin = role == UserRole.ADMIN;
    
    if (!isOwner && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 문의를 조회할 권한이 없습니다.");
    }
    
    return CsTicketResponse.from(ticket);
  }

  @Transactional
  public CsTicketReplyResponse createReply(Long csTicketId, Long responderId, CsTicketReplyRequest req) {
    // 답변자 권한 확인
    Member responder = memberRepository.findById(responderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "답변자 정보를 찾을 수 없습니다."));
    
    if (responder.getMemberRole() != UserRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "답변 권한이 없습니다.");
    }
    
    CsTicket ticket = csTicketRepository.findById(csTicketId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 내역을 찾을 수 없습니다."));
    
    CsTicketReply reply = CsTicketReply.create(ticket, responderId, req.replyContent());
    csTicketReplyRepository.save(reply);
    
    return CsTicketReplyResponse.from(reply);
  }

  // 관리자용: 전체 티켓 조회
  @Transactional(readOnly = true)
  public Page<CsTicketResponse> getAllTickets(UserRole role, int page, int size, String sort) {
    if (role != UserRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 전체 문의를 조회할 수 있습니다.");
    }
    
    String[] sortParams = sort.split(",");
    String sortField = sortParams[0];
    Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
        ? Sort.Direction.ASC 
        : Sort.Direction.DESC;
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
    Page<CsTicket> tickets = csTicketRepository.findAll(pageable);
    return tickets.map(CsTicketResponse::from);
  }

  // 티켓의 답변 목록 조회
  @Transactional(readOnly = true)
  public java.util.List<CsTicketReplyResponse> getReplies(Long csTicketId, Long memberId, UserRole role) {
    CsTicket ticket = csTicketRepository.findById(csTicketId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 내역을 찾을 수 없습니다."));
    
    // 본인의 티켓이거나 ADMIN인 경우에만 조회 가능
    boolean isOwner = ticket.getMember().getMemberId().equals(memberId);
    boolean isAdmin = role == UserRole.ADMIN;
    
    if (!isOwner && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 문의의 답변을 조회할 권한이 없습니다.");
    }
    
    return csTicketReplyRepository.findByCsTicket_CsTicketIdOrderByReplyCreatedAtAsc(csTicketId)
        .stream()
        .map(CsTicketReplyResponse::from)
        .toList();
  }

  // 관리자용: 티켓 상태 변경
  @Transactional
  public CsTicketResponse changeTicketStatus(Long csTicketId, Long memberId, UserRole role, String status) {
    if (role != UserRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 상태를 변경할 수 있습니다.");
    }
    
    CsTicket ticket = csTicketRepository.findById(csTicketId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 내역을 찾을 수 없습니다."));
    
    try {
      com.twog.shopping.domain.support.entity.TicketStatus newStatus = 
          com.twog.shopping.domain.support.entity.TicketStatus.valueOf(status);
      ticket.changeStatus(newStatus);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다: " + status);
    }
    
    return CsTicketResponse.from(ticket);
  }
}
