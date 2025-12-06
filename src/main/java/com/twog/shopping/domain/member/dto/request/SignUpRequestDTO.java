package com.twog.shopping.domain.member.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SignUpRequestDTO {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식을 입력해야 합니다.")
    private String memberEmail;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String memberName;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해야 합니다.")
    private String memberPwd;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]{11}$", message = "전화번호는 숫자 11자리여야 합니다.")
    private String memberPhone;

    @NotNull(message = "생년월일은 필수 입력값입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate memberBirth;

    @NotNull(message = "성별은 필수 입력값입니다.")
    @Pattern(regexp = "M|F", message = "성별은 M 또는 F만 입력 가능합니다.")
    private String memberGender;

    private String role;

    // ===== 프로필 정보 =====

    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String profileAddress;

    @NotBlank(message = "상세 주소는 필수 입력값입니다.")
    private String profileDetailAddress;

    @NotBlank(message = "선호 카테고리는 필수 입력값입니다.")
    private String profilePreferred;

    @NotBlank(message = "관심 카테고리는 필수 입력값입니다.")
    private String profileInterests;

}
