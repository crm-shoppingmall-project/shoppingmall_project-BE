package com.twog.shopping.domain.member.controller;

import com.twog.shopping.domain.member.dto.TokenDTO;
import com.twog.shopping.domain.member.dto.request.*;
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

    @PutMapping("/mypage/update")
    public ResponseEntity<MemberProfileResponseDTO> updateMyPage(@AuthenticationPrincipal DetailsUser detailsUser
                                                            , @Valid @RequestBody MemberProfileRequestDTO dto) {


       String email = detailsUser.getUsername();

        MemberProfileResponseDTO response = memberProfileService.updateMyPage(email, dto);


        return ResponseEntity.ok(response);

    }


    @PatchMapping("/mypage/changePwd")
    public ResponseEntity<Void> changePwd(
            @AuthenticationPrincipal DetailsUser detailsUser,
            @Valid @RequestBody PwdChangeRequestDTO dto ){

        String email = detailsUser.getUsername();
        memberProfileService.changePwd(email,dto);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/mypage/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal DetailsUser detailsUser,
            @Valid @RequestBody MemberWithdrawnRequestDTO dto
     ){
        String email = detailsUser.getUsername();
        memberProfileService.withdraw(email,dto);

        return ResponseEntity.noContent().build();
    }

}
