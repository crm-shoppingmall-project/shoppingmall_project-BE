package com.twog.shopping.domain.analytics.repository;

import com.twog.shopping.domain.analytics.entity.MemberGradeTarget;
import com.twog.shopping.domain.analytics.entity.MemberRfm;
import com.twog.shopping.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRfmRepository extends JpaRepository<MemberRfm,Long> {

    Optional<MemberRfm> findByMember(Member member);

    @Query("""
    SELECT 
            m.memberId as memberId,
            mr.rfmTotalScore as rfmTotalScore,
            g.gradeName as currentGrade
      FROM MemberRfm mr
      JOIN mr.member m  
      LEFT JOIN m.memberGrade g
    """)
    Page<MemberGradeTarget> findGradeTargets(Pageable pageable);
}
