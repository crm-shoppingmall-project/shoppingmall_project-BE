package com.twog.shopping.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TokenDTO {

    private String grantType;   // 토큰 타입 (예: "Bearer")
    private String accessToken; // 실제 토큰 문자열
    private Long accessTokenExpiresIn; // 만료 시간


}