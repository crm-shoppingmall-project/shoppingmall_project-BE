package com.twog.shopping.domain.member.controller;

import com.twog.shopping.domain.member.dto.TokenDTO;
import com.twog.shopping.domain.member.dto.request.LoginRequestDTO;
import com.twog.shopping.domain.member.dto.request.SignUpRequestDTO;
import com.twog.shopping.domain.member.service.MemberService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        memberService.signup(signUpRequestDTO);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {

        try{
            TokenDTO tokenDTO = memberService.login(loginRequestDTO);

            return ResponseEntity.ok(tokenDTO);
        }catch(RuntimeException e){
            return ResponseEntity.status(401).body(e.getMessage());
        }

    }
}
