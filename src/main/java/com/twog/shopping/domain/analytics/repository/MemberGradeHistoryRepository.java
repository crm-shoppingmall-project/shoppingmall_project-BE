package com.twog.shopping.domain.analytics.repository;

import com.twog.shopping.domain.analytics.entity.MemberGradeHistory;
import com.twog.shopping.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberGradeHistoryRepository extends JpaRepository<MemberGradeHistory,Long> {

    Optional<MemberGradeHistory> findTopByMemberOrderByHistoryChangedDesc(Member member);
}
