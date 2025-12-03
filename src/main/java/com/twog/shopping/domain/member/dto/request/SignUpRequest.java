package com.twog.shopping.domain.member.dto.request;

import com.twog.shopping.domain.member.entity.Member;
import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String address;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .username(this.username)
                .password(encodedPassword)
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .address(this.address)
                .build();
    }
}
