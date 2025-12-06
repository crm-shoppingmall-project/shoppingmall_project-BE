package com.twog.shopping.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberWithdrawnRequestDTO {

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String memberPwd;

}
