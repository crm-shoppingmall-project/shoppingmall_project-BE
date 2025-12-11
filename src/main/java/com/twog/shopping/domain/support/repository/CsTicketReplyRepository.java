package com.twog.shopping.domain.support.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.twog.shopping.domain.support.entity.CsTicketReply;

public interface CsTicketReplyRepository extends JpaRepository<CsTicketReply, Long> {
    // 특정 티켓의 답변 목록 조회
    List<CsTicketReply> findByCsTicket_CsTicketIdOrderByReplyCreatedAtAsc(Long csTicketId);
}
