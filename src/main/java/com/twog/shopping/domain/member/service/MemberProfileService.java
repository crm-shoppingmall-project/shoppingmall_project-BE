package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.entity.MemberProfile;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class MemberProfileService {

    private final MemberProfileRepository memberProfileRepository;

    public MemberProfileService(MemberProfileRepository memberProfileRepository) {
        this.memberProfileRepository = memberProfileRepository;
    }

    public MemberProfile findByMemberId(Long memberId) {
        return memberProfileRepository.findByMember_MemberId(memberId);
    }

}
