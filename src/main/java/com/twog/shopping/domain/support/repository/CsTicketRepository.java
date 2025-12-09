package com.twog.shopping.domain.support.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.twog.shopping.domain.support.entity.CsTicket;

public interface CsTicketRepository extends JpaRepository<CsTicket, Long> {
    
    // 특정 회원의 문의 내역 조회 (페이징)
    Page<CsTicket> findByMember_MemberId(Long memberId, Pageable pageable);
}
