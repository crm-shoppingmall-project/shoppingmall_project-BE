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
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberGradeRepository memberGradeRepository;

    public MemberService(MemberRepository memberRepository, MemberProfileRepository memberProfileRepository, PasswordEncoder passwordEncoder,MemberGradeRepository memberGradeRepository) {
        this.memberRepository = memberRepository;
        this.memberProfileRepository = memberProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberGradeRepository = memberGradeRepository;
    }

    @Transactional
    public MemberResponseDTO signup(SignUpRequestDTO signUpRequestDTO) {
        // 1. User 엔티티 생성 (DTO -> Entity 변환)

        MemberGrade defaultGrade = memberGradeRepository.findByGradeName(GradeName.BRONZE);

        boolean exists = memberRepository.existsByMemberEmail(signUpRequestDTO.getMemberEmail());

        if(exists){
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        Member member = Member.builder()
                .memberEmail(signUpRequestDTO.getMemberEmail())
                .memberName(signUpRequestDTO.getMemberName())
                .memberPhone(signUpRequestDTO.getMemberPhone())
                .memberGender(signUpRequestDTO.getMemberGender().charAt(0))
                .memberBirth(signUpRequestDTO.getMemberBirth())
                .memberGrade(defaultGrade)
                .memberStatus(MemberStatus.active)
                .memberRole(UserRole.USER)
                .memberPwd(passwordEncoder.encode(signUpRequestDTO.getMemberPwd()))
                .build();

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
                .orElseThrow(()-> new RuntimeException("존재하지 않는 이메일 입니다."));

        if(!passwordEncoder.matches(loginRequestDTO.getMemberPwd(),member.getMemberPwd())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        String token = TokenUtils.generateJwtToken(member);

        Long expireTime = System.currentTimeMillis() + TokenUtils.getTokenValidateTime();

        return new TokenDTO("Bearer", token, expireTime);
    }


    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByMemberEmail(email);
    }


    public Member getByEmailOrThrow(String email) {
        return memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다: " + email));
    }

}