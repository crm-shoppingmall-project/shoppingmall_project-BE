package com.twog.shopping.domain.support.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.support.dto.CsTicketRequest;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.entity.CsTicket;
import com.twog.shopping.domain.support.repository.CsTicketRepository;

@Service
public class CsTicketService {

  private final CsTicketRepository csTicketRepository;

  public CsTicketService(CsTicketRepository csTicketRepository) {
    this.csTicketRepository = csTicketRepository;
  }

  @Transactional
  public CsTicketResponse createTicket(CsTicketRequest req, Member member) {

    CsTicket ticket = CsTicket.create(
      member,
      req.csTicketChannel(),
      req.csTicketCategory(),
      req.csTicketTitle(),
      req.csTicketContent()
    );

    csTicketRepository.save(ticket);

    return CsTicketResponse.from(ticket);
  }

  @Transactional(readOnly = true)
  public Page<CsTicketResponse> getMyTickets(Long memberId, Pageable pageable) {
    Page<CsTicket> tickets = csTicketRepository.findByMember_MemberId(memberId, pageable);
    return tickets.map(CsTicketResponse::from);
  }
}
