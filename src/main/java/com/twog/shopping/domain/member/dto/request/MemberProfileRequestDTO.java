package com.twog.shopping.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@NoArgsConstructor
public class MemberProfileRequestDTO {

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String memberName;
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]{11}$", message = "전화번호는 숫자 11자리여야 합니다.")
    private String memberPhone;

    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String profileAddress;
    @NotBlank(message = "상세 주소는 필수 입력값입니다.")
    private String profileDetailAddress;
    @NotBlank(message = "선호 카테고리는 필수 입력값입니다.")
    private String profilePreferred;
    @NotBlank(message = "관심 카테고리는 필수 입력값입니다.")
    private String profileInterests;
}
