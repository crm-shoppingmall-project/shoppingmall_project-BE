package com.twog.shopping.domain.member.dto.request;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String memberEmail;
    private String memberPwd;
}
