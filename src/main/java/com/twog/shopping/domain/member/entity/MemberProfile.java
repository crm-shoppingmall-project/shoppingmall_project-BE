package com.twog.shopping.domain.member.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_memberProfile")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MemberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "profile_address")
    private String profileAddress;

    @Column(name = "profile_detail_address")
    private String profileDetailAddress;

    @Column(name = "profile_preffered")
    private String profilePreffered;

    @Column(name = "profile_interests")
    private String profileInterests;


}
