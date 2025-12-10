package com.twog.shopping.domain.member.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileResponseDTO {

    private String memberName;
    private String memberPhone;

    private String profileAddress;
    private String profileDetailAddress;
    private String profilePreferred;
    private String profileInterests;

    // 회원 등급
    private String gradeName;
    private LocalDate memberBirth;
}
