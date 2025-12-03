//package com.twog.shopping.domain.member.service;
//
//import com.twog.shopping.domain.member.dto.request.LoginRequest;
//import com.twog.shopping.domain.member.dto.request.SignUpRequest;
//import com.twog.shopping.domain.member.entity.Member;
//import com.twog.shopping.domain.member.repository.MemberRepository;
//import com.twog.shopping.global.common.util.PasswordEncoder;
//import com.twog.shopping.global.error.ErrorCode;
//import com.twog.shopping.global.error.exception.CustomException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class MemberService {
//
//    private final MemberRepository memberRepository;
//
//    @Transactional
//    public void signUp(SignUpRequest signUpRequest) {
//        if (memberRepository.existsByUsername(signUpRequest.getUsername())) {
//            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
//        }
//
//        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
//        Member member = signUpRequest.toEntity(encodedPassword);
//        memberRepository.save(member);
//    }
//
//    public String login(LoginRequest loginRequest) {
//        Member member = memberRepository.findByUsername(loginRequest.getUsername())
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
//
//        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
//            throw new CustomException(ErrorCode.INVALID_PASSWORD);
//        }
//
//        // 간단한 토큰 생성 (실제로는 JWT 등을 사용해야 함)
//        return "token-for-" + member.getUsername();
//    }
//}
