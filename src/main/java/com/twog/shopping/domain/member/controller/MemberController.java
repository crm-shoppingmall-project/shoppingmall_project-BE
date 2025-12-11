package com.twog.shopping.domain.member.controller;

import com.twog.shopping.domain.member.dto.TokenDTO;
import com.twog.shopping.domain.member.dto.request.*;
import com.twog.shopping.domain.member.dto.response.MemberProfileResponseDTO;
import com.twog.shopping.domain.member.dto.response.MemberResponseDTO;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.member.service.MemberProfileService;
import com.twog.shopping.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member API", description = "회원 가입, 로그인, 마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberProfileService memberProfileService;

    @Operation(summary = "회원가입", description = "사용자 정보를 받아 회원가입을 처리합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        MemberResponseDTO response = memberService.signup(signUpRequestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 처리하고 JWT를 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            TokenDTO tokenDTO = memberService.login(loginRequestDTO);
            return ResponseEntity.ok(tokenDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @Operation(summary = "내 정보 조회 (마이페이지)", description = "인증된 사용자의 마이페이지 정보를 조회합니다.")
    @GetMapping("/mypage/me")
    public ResponseEntity<MemberProfileResponseDTO> getMyPage(@AuthenticationPrincipal DetailsUser detailsUser) {
        String email = detailsUser.getUsername();
        MemberProfileResponseDTO response = memberProfileService.getMyPageInfo(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 정보 수정", description = "인증된 사용자의 프로필 정보를 수정합니다.")
    @PutMapping("/mypage/update")
    public ResponseEntity<MemberProfileResponseDTO> updateMyPage(@AuthenticationPrincipal DetailsUser detailsUser,
                                                                 @Valid @RequestBody MemberProfileRequestDTO dto) {
        String email = detailsUser.getUsername();
        MemberProfileResponseDTO response = memberProfileService.updateMyPage(email, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "인증된 사용자의 비밀번호를 변경합니다.")
    @PatchMapping("/mypage/changePwd")
    public ResponseEntity<Void> changePwd(@AuthenticationPrincipal DetailsUser detailsUser,
                                          @Valid @RequestBody PwdChangeRequestDTO dto) {
        String email = detailsUser.getUsername();
        memberProfileService.changePwd(email, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 탈퇴", description = "인증된 사용자를 탈퇴 처리합니다.")
    @DeleteMapping("/mypage/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal DetailsUser detailsUser,
                                         @Valid @RequestBody MemberWithdrawnRequestDTO dto) {
        String email = detailsUser.getUsername();
        memberProfileService.withdraw(email, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "관리자용 전체 회원 조회", description = "관리자가 모든 회원 정보를 조회합니다.")
    @GetMapping("/admin/all")
    public ResponseEntity<java.util.List<com.twog.shopping.domain.member.dto.response.MemberAdminResponseDTO>> getAllMembers(
            @AuthenticationPrincipal DetailsUser detailsUser) {
        return ResponseEntity.ok(memberService.getAllMembersWithRfm());
    }
}
