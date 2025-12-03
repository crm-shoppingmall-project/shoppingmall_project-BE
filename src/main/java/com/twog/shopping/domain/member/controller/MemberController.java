package com.twog.shopping.domain.member.controller;

import com.twog.shopping.domain.member.dto.request.LoginRequest;
import com.twog.shopping.domain.member.dto.request.SignUpRequest;
import com.twog.shopping.domain.member.service.MemberService;
import com.twog.shopping.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody SignUpRequest signUpRequest) {
        memberService.signUp(signUpRequest);
        return ResponseEntity.ok(ApiResponse.of("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        String token = memberService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.of("로그인이 완료되었습니다.", token));
    }
}
