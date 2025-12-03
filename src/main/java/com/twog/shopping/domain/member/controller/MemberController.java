package com.twog.shopping.domain.member.controller;

import com.twog.shopping.domain.member.dto.request.LoginRequestDTO;
import com.twog.shopping.domain.member.dto.request.SignUpRequestDTO;
import com.twog.shopping.domain.member.service.DetailsUser;
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

    private final DetailsUser detailsUser;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        detailsUser.signUp(signUpRequestDTO);
        return ResponseEntity.ok(ApiResponse.of("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        String token = detailsUser.login(loginRequestDTO);
        return ResponseEntity.ok(ApiResponse.of("로그인이 완료되었습니다.", token));
    }
}
