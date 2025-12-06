package com.twog.shopping.domain.member.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
public class MemberProfileRequestDTO {

    private String memberName;
    private String memberPhone;

    private String profileAddress;
    private String profileDetailAddress;
    private String profilePreferred;
    private String profileInterests;
}
