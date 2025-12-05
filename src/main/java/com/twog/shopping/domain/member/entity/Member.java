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
    private MemberStatus memberStatus = MemberStatus.active;

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

//    public static Member createMember(
//            String memberEmail,
//            String memberName,
//            String memberPhone,
//            char memberGender,
//            LocalDate memberBirth,
//            String memberPwd
//    ){
//        LocalDateTime now = LocalDateTime.now();
//
//        return Member.builder()
//
//
//
//    }





    @PrePersist
    public void onCreate(){
        if(this.memberCreated == null){
            this.memberCreated = LocalDateTime.now();
        }
        if(this.memberUpdated == null){
            this.memberUpdated = this.memberCreated;
        }

        if(this.memberLastAt == null){
            this.memberLastAt = LocalDateTime.now();  // 마지막 로그인 날짜
        }


    }

    @PreUpdate
    public void onUpdate(){
        this.memberUpdated = LocalDateTime.now();

    }






}
