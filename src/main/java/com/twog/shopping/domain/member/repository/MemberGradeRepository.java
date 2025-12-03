package com.twog.shopping.domain.member.repository;

import com.twog.shopping.domain.member.entity.MemberGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGradeRepository extends JpaRepository<MemberGrade,Integer> {

    boolean existsByMemberEmail(String memberEmail);

    boolean existsByMemberPhone(String memberPhone);



}
