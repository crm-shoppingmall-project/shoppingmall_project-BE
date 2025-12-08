package com.twog.shopping.domain.analytics.repository;

import com.twog.shopping.domain.analytics.entity.MemberRfm;
import com.twog.shopping.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRfmRepository extends JpaRepository<MemberRfm,Long> {

    Optional<MemberRfm> findByMember(Member member);
}
