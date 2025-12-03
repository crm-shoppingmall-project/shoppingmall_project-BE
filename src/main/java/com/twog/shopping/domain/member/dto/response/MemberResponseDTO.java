package com.twog.shopping.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MemberResponseDTO {

    private Long memberId;
    private String memberEmail;
    private String memberName;
    private String memberPhone;
    private char memberGender;
    private LocalDate memberBirth;

    private String profileAddress;
    private String profileDetailAddress;
    private String profilePreferred;
    private String profileInterests;
}
