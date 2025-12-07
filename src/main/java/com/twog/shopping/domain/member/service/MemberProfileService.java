package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.dto.request.MemberProfileRequestDTO;
import com.twog.shopping.domain.member.dto.request.MemberWithdrawnRequestDTO;
import com.twog.shopping.domain.member.dto.request.PwdChangeRequestDTO;
import com.twog.shopping.domain.member.dto.response.MemberProfileResponseDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberProfile;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberProfileRepository memberProfileRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

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
    public MemberProfileResponseDTO updateMyPage(String email, MemberProfileRequestDTO dto) {

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다. " + email));

        member.updateMember(dto.getMemberName(),dto.getMemberPhone());
        member.getMemberProfile().updateMemberInfo(dto.getProfileAddress(),dto.getProfileDetailAddress(),dto.getProfilePreferred(),dto.getProfileInterests());

        MemberProfile profile = member.getMemberProfile();

        MemberProfileResponseDTO response = modelMapper.map(member,MemberProfileResponseDTO.class);
        modelMapper.map(profile,response);

        return response;

    }

    @Transactional
    public void changePwd(String email, PwdChangeRequestDTO dto){

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원정보를 찾을 수 없습니다." + email));

        if(!passwordEncoder.matches(dto.getOldPassword(),member.getMemberPwd())){

            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다." + member.getMemberPwd());
        }

        if(!dto.getNewPassword().equals(dto.getConfirmPassword())){
            throw new RuntimeException("새 비밀번호가 일치하지 않습니다.");
        }

        if(passwordEncoder.matches(dto.getNewPassword(),member.getMemberPwd())){
            throw new RuntimeException("기본 비밀번호와 다른 비밀번호를 사용해 주세요.");
        }

        String encoded = passwordEncoder.encode(dto.getNewPassword());
        member.changePassword(encoded);

    }

    @Transactional
    public void withdraw(String email, MemberWithdrawnRequestDTO dto){

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(()-> new RuntimeException("회원정보를 찾을 수 없습니다." + email));

        if(!passwordEncoder.matches(dto.getMemberPwd(),member.getMemberPwd())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        member.withdraw();

    }


}
