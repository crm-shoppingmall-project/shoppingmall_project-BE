package com.twog.shopping.global.auth.handler;

import com.twog.shopping.domain.member.service.DetailsService;
import com.twog.shopping.domain.member.service.DetailsUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 비밀번호 매칭을 해서 토큰을 반환해 주는 역할
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private DetailsService detailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 로그인 요청이 들어오면 UsernamePasswordAuthenticationToken 객체로 감싸져 전달
        UsernamePasswordAuthenticationToken loginToken = (UsernamePasswordAuthenticationToken) authentication;
        // 토큰에서 아이디와 비밀번호 추출
        String id = loginToken.getName();
        String pass = (String) loginToken.getCredentials();
        // loadUserByUsername 메소드로 DB에서 해당 아이디를 가진 사용자 정보 로드
        DetailsUser detailsUser = (DetailsUser) detailsService.loadUserByUsername(id);

        // 입력한 비밀번호와 DB에 저장된 비밀번호를 passwordEncoder.matches()로 비교
        if(!passwordEncoder.matches(pass, detailsUser.getPassword())) {
            throw new BadCredentialsException(pass + "는 틀린 비밀번호입니다.");
        }
        // 인증 성공 시, 인증된 사용자 정보를 담은 새로운 UsernamePasswordAuthenticationToken 객체 반환
        return new UsernamePasswordAuthenticationToken(detailsUser, pass, detailsUser.getAuthorities());
    }

    // 이 provider가 어떤 타입의 인증 객체를 처리할지 지정
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}