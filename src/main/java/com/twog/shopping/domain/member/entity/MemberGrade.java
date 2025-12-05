package com.twog.shopping.domain.member.entity;

import com.twog.shopping.global.common.entity.GradeName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Builder
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
