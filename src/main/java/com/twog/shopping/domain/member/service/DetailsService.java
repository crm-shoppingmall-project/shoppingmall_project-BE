package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.entity.Member;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DetailsService implements UserDetailsService {

    private final MemberService memberService;

    public DetailsService(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 로그인 요청 시 사용자의 "아이디(또는 이메일)"를 받아
     * DB에서 Member를 조회하고, 스프링 시큐리티에서 쓸 UserDetails로 감싸서 반환
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("이메일이 비어 있습니다.");
        }

        // 🔹 이메일로 Member 조회
        Member member = memberService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 회원을 찾을 수 없습니다: " + email));

        // 🔹 아까 만든 MemberDetails 사용
        return new DetailsUser(member);
    }
}