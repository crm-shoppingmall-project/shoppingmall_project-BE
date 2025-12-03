package com.twog.shopping.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_memberGradeHistory")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class MemberGradeHistory{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_code")
    private MemberGrade memberGrade;

    @Column(name = "history_before")
    private String historyBefore;

    @Column(name = "history_after")
    private String historyAfter;

    @Column(name = "history_changed")
    private LocalDate historyChanged;






}
