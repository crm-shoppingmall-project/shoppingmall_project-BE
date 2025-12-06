package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.dto.request.MemberProfileRequestDTO;
import com.twog.shopping.domain.member.dto.response.MemberProfileResponseDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberProfile;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberProfileRepository memberProfileRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public MemberProfile findByMemberId(Long memberId) {
        return memberProfileRepository.findByMember_MemberId(memberId);
    }

    @Transactional(readOnly = true)
    public MemberProfileResponseDTO getMyPageInfo(String email){

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다." + email));

        MemberProfile profile = member.getMemberProfile();

        MemberProfileResponseDTO response = modelMapper.map(member,MemberProfileResponseDTO.class);

        if (profile != null) {
            modelMapper.map(profile, response);
        }

        return response;

    }

    @Transactional
    public MemberResponseDTO updateMyPage(String email, MemberProfileRequestDTO dto) {

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다. " + email));

        member.updateMember(dto.getMemberName(),dto.getMemberPhone());
        member.getMemberProfile().updateMemberInfo(dto.getProfileAddress(),dto.getProfileDetailAddress(),dto.getProfilePreferred(),dto.getProfileInterests());

        MemberResponseDTO response = modelMapper.map(member,MemberResponseDTO.class);
        modelMapper.map(member.getMemberProfile(),response);

        return response;

    }
}
