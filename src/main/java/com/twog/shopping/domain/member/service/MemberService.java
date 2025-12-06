package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.dto.TokenDTO;
import com.twog.shopping.domain.member.dto.request.LoginRequestDTO;
import com.twog.shopping.domain.member.dto.request.SignUpRequestDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.entity.*;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.global.common.entity.GradeName;
import com.twog.shopping.global.common.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberGradeRepository memberGradeRepository;

    // 회원가입
    @Transactional
    public MemberResponseDTO signup(SignUpRequestDTO signUpRequestDTO) {
        // 1. User 엔티티 생성 (DTO -> Entity 변환)

        MemberGrade defaultGrade = memberGradeRepository.findByGradeName(GradeName.BRONZE);

        boolean exists = memberRepository.existsByMemberEmail(signUpRequestDTO.getMemberEmail());

        if(exists){
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        Member member = Member.createNewMember(
                    defaultGrade,
                        signUpRequestDTO.getMemberEmail(),
                        signUpRequestDTO.getMemberName(),
                        signUpRequestDTO.getMemberPhone(),
                        signUpRequestDTO.getMemberGender().charAt(0),
                        signUpRequestDTO.getMemberBirth(),
                        passwordEncoder.encode(signUpRequestDTO.getMemberPwd())
                );


        memberRepository.save(member);

        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .profileAddress(signUpRequestDTO.getProfileAddress())
                .profileDetailAddress(signUpRequestDTO.getProfileDetailAddress())
                .profilePreferred(signUpRequestDTO.getProfilePreferred())
                .profileInterests(signUpRequestDTO.getProfileInterests())
                .build();

        memberProfileRepository.save(profile);


        return MemberResponseDTO.builder()
                .memberId(member.getMemberId())
                .memberEmail(member.getMemberEmail())
                .memberName(member.getMemberName())
                .memberPhone(member.getMemberPhone())
                .memberGender(member.getMemberGender())
                .memberBirth(member.getMemberBirth())

                .profileAddress(profile.getProfileAddress())
                .profileDetailAddress(profile.getProfileDetailAddress())
                .profilePreferred(profile.getProfilePreferred())
                .profileInterests(profile.getProfileInterests())
                .build();

    }



    // 로그인
    public TokenDTO login(LoginRequestDTO loginRequestDTO){

        Member member = memberRepository.findByMemberEmail(loginRequestDTO.getMemberEmail())
                .orElseThrow(()-> new RuntimeException("이메일 혹은 비밀번호가 올바르지 않습니다."));

        if(member.getMemberStatus() == MemberStatus.withdrawn) {
            throw new RuntimeException("탈퇴한 회원입니다. 다시 가입해주세요.");
        }

        if(!passwordEncoder.matches(loginRequestDTO.getMemberPwd(),member.getMemberPwd())){
            throw new RuntimeException("이메일 혹은 비밀번호가 올바르지 않습니다.");
        }

        String token = TokenUtils.generateJwtToken(member);

        Long expireTime = System.currentTimeMillis() + TokenUtils.getTokenValidateTime();

        return new TokenDTO("Bearer", token, expireTime);
    }


    // 존재여부만 확인할떄
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByMemberEmail(email);
    }


    @Transactional(readOnly = true)
    public Member getByEmailOrThrow(String email) {
        return memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다: " + email));
    }

}