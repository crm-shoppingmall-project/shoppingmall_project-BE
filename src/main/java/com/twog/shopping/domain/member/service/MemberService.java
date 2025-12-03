package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.dto.request.SignUpRequestDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberProfile;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.global.common.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberProfileRepository memberProfileRepository;

    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder, MemberProfileRepository memberProfileRepository ) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.memberProfileRepository = memberProfileRepository;
    }

    @Transactional
    public MemberResponseDTO signup(SignUpRequestDTO signUpRequestDTO) {
        // 1. User 엔티티 생성 (DTO -> Entity 변환)
        Member member = Member.builder()
                .memberEmail(signUpRequestDTO.getMemberEmail())
                .memberName(signUpRequestDTO.getMemberName())
                .memberPhone(signUpRequestDTO.getMemberPhone())
                .memberGender(signUpRequestDTO.getMemberGender())
                .memberBirth(signUpRequestDTO.getMemberBirth())
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

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByMemberEmail(email);
    }

    public Member getByEmailOrThrow(String email) {
        return memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다: " + email));
    }

}