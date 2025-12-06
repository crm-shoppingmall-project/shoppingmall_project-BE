package com.twog.shopping.domain.member.entity;

import com.twog.shopping.global.common.entity.GradeName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id",nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_code",nullable = false)
    private MemberGrade memberGrade;

    @Column(name = "member_name",nullable = false)
    private String memberName;

    @Column(name = "member_gender",nullable = false, length = 1)
    private char memberGender;

    @Column(name = "member_phone",nullable = false , length = 11)
    private String memberPhone;

    @Column(name = "member_birth",nullable = false)
    private LocalDate memberBirth;

    @Column(name = "member_pwd",nullable = false)
    private String memberPwd;

    @Column(name = "member_email",nullable = false)
    private String memberEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status",nullable = false)
    private MemberStatus memberStatus;

    @Column(name = "member_created",nullable = false)
    private LocalDateTime memberCreated = LocalDateTime.now();

    @Column(name = "member_updated",nullable = false)
    private LocalDateTime memberUpdated = LocalDateTime.now();

    @Column(name = "member_withdrawn")
    private LocalDateTime memberWithDrawn;

    @Column(name = "member_last_at",nullable = false)
    private LocalDateTime memberLastAt = LocalDateTime.now();

    @Column(name = "member_role",nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRole memberRole;

    public static Member createNewMember(

            MemberGrade grade,
            String memberEmail,
            String memberName,
            String memberPhone,
            char memberGender,
            LocalDate memberBirth,
            String encodePwd
    ){
        LocalDateTime now = LocalDateTime.now();

        return Member.builder()
                .memberGrade(grade)
                .memberEmail(memberEmail)
                .memberName(memberName)
                .memberPhone(memberPhone)
                .memberGender(memberGender)
                .memberBirth(memberBirth)
                .memberPwd(encodePwd)
                .memberStatus(MemberStatus.active)
                .memberRole(UserRole.USER)
                .memberCreated(now)
                .memberUpdated(now)
                .memberLastAt(now)
                .build();
    }

    public void changeGrade(MemberGrade newGrade){
        this.memberGrade = newGrade;
    }

    public void changeStaus(MemberStatus newStatus){
        this.memberStatus = newStatus;
    }

    public void withdraw(){
        this.memberStatus = MemberStatus.withdrawn;
        this.memberWithDrawn = LocalDateTime.now();
    }

    public void updateLastLogin(){
        this.memberLastAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPwd){
        this.memberPwd = encodedPwd;
    }

    public void changePhone(String newPhone){
        this.memberPhone = newPhone;
    }


    @PreUpdate
    public void onUpdate(){
        this.memberUpdated = LocalDateTime.now();

    }

}
