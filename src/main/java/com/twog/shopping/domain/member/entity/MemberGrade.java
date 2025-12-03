package com.twog.shopping.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_memberGrade")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MemberGrade {

    @Id
    @Column(name = "grade_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gradeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_name")
    private GradeName gradeName;

    @Column(name = "grade_desc")
    private String gradeDesc;
}
