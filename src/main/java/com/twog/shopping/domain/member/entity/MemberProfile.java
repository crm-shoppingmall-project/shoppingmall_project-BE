package com.twog.shopping.domain.member.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "member_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Builder
public class MemberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_seq")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id" , nullable = false)
    private Member member;

    @Column(name = "profile_address", nullable = false)
    private String profileAddress;

    @Column(name = "profile_detail_address")
    private String profileDetailAddress;

    @Column(name = "profile_preferred")
    private String profilePreferred;

    @Column(name = "profile_interests")
    private String profileInterests;

    public void updateMemberInfo(String address, String detailAddress, String preferred, String interests){
        this.profileAddress = address;
        this.profileDetailAddress = detailAddress;
        this.profilePreferred = preferred;
        this.profileInterests = interests;
    }


}
