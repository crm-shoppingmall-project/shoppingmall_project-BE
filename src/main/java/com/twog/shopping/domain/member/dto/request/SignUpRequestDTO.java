package com.twog.shopping.domain.member.dto.request;

import com.twog.shopping.domain.member.entity.Member;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignUpRequestDTO {
    private String memberEmail;
    private String memberName;
    private String memberPwd;
    private String memberPhone;
    private LocalDate memberBirth;
    private char memberGender;
    private String role;

    private String profileAddress;
    private String profileDetailAddress;
    private String profilePreferred;
    private String profileInterests;


}
