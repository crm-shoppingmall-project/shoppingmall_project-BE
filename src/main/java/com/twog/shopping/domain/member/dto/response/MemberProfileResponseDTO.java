package com.twog.shopping.domain.member.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberProfileResponseDTO {

    private String memberName;
    private String memberPhone;

    private String profileAddress;
    private String profileDetailAddress;
    private String profilePreferred;
    private String profileInterests;
}
