package com.twog.shopping.domain.member.repository;

import com.twog.shopping.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findByMemberEmail(String memberEmail);

//    Optional<Member> findByMemberStatus(MemberStatus memberStatus);

    boolean existsByMemberEmail(String memberEmail);
}
