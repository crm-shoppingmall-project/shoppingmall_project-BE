package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.dto.TokenDTO;
import com.twog.shopping.domain.member.dto.request.LoginRequestDTO;
import com.twog.shopping.domain.member.dto.request.SignUpRequestDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.entity.*;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.global.common.utils.TokenUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberProfileRepository memberProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberGradeRepository memberGradeRepository;

    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder, MemberProfileRepository memberProfileRepository, PasswordEncoder passwordEncoder,MemberGradeRepository memberGradeRepository) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.memberProfileRepository = memberProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberGradeRepository = memberGradeRepository;
    }

    @Transactional
    public MemberResponseDTO signup(SignUpRequestDTO signUpRequestDTO) {
        // 1. User 엔티티 생성 (DTO -> Entity 변환)

        MemberGrade defaultGrade = memberGradeRepository.findByGradeName(GradeName.BRONZE);



        Member member = Member.builder()
                .memberEmail(signUpRequestDTO.getMemberEmail())
                .memberName(signUpRequestDTO.getMemberName())
                .memberPhone(signUpRequestDTO.getMemberPhone())
                .memberGender(signUpRequestDTO.getMemberGender())
                .memberBirth(signUpRequestDTO.getMemberBirth())
                .memberGrade(defaultGrade)
                .memberStatus(MemberStatus.ACTIVE)
                .memberRole( // 권한 설정
                        (signUpRequestDTO.getRole() != null && !signUpRequestDTO.getRole().isEmpty())
                                ? UserRole.valueOf(signUpRequestDTO.getRole())
                                : UserRole.USER
                )
                .memberPwd(bCryptPasswordEncoder.encode(signUpRequestDTO.getMemberPwd()))
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

        // 3. 권한 설정 로직 수정
        // 요청에 role이 있으면 그걸 쓰고, 없으면 기본값 USER로 설정
        if (signUpRequestDTO.getRole() != null && !signUpRequestDTO.getRole().isEmpty()) {
            // String("ADMIN") -> Enum(UserRole.ADMIN)으로 변환
            try {
                member.setMemberRole(UserRole.valueOf(signUpRequestDTO.getRole()));
            } catch (IllegalArgumentException e) {
                // 이상한 권한(SUPER_MAN 등)이 들어오면 그냥 USER로 설정
                member.setMemberRole(UserRole.USER);
            }
        } else {
            member.setMemberRole(UserRole.USER);
        }

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