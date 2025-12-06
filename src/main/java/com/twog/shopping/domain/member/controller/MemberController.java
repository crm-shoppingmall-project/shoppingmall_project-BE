package com.twog.shopping.domain.member.controller;

import com.twog.shopping.domain.member.dto.TokenDTO;
import com.twog.shopping.domain.member.dto.request.LoginRequestDTO;
import com.twog.shopping.domain.member.dto.request.MemberProfileRequestDTO;
import com.twog.shopping.domain.member.dto.request.SignUpRequestDTO;
import com.twog.shopping.domain.member.dto.response.MemberProfileResponseDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.member.service.MemberProfileService;
import com.twog.shopping.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberProfileService memberProfileService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        MemberResponseDTO response = memberService.signup(signUpRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {

        try {
            TokenDTO tokenDTO = memberService.login(loginRequestDTO);

            return ResponseEntity.ok(tokenDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

    }

    @GetMapping("/mypage/me")
    public ResponseEntity<MemberProfileResponseDTO> getMyPage(@AuthenticationPrincipal DetailsUser detailsUser) {

        String email = detailsUser.getUsername();

        MemberProfileResponseDTO response = memberProfileService.getMyPageInfo(email);

        return ResponseEntity.ok(response);

    }

    @PutMapping("/mypage/update/{memberId}")
    public ResponseEntity<MemberResponseDTO> updateMyPage(@AuthenticationPrincipal DetailsUser detailsUser
                                                            , @Valid @RequestBody MemberProfileRequestDTO dto
                                                            , @PathVariable Long memberId) {

        Member loginMember = detailsUser.getMember();

        if (!loginMember.getMemberId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MemberResponseDTO response = memberProfileService.updateMyPage(loginMember.getMemberEmail(),dto);


        return ResponseEntity.ok(response);

    }


}
