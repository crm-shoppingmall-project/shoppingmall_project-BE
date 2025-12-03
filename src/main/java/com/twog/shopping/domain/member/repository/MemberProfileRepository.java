package com.twog.shopping.domain.member.repository;

import com.twog.shopping.domain.member.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile,Long> {


    MemberProfile findByMember_MemberId(Long memberId);


}
