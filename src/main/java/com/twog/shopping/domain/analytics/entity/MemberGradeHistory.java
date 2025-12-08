package com.twog.shopping.domain.analytics.entity;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.global.common.entity.GradeName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_grade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberGradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_code", nullable = false)
    private MemberGrade grade;

    @Column(name = "history_before", nullable = false, length = 20)
    private GradeName historyBefore;

    @Column(name = "history_after", nullable = false, length = 20)
    private GradeName historyAfter;

    @Column(name = "history_changed", nullable = false)
    private LocalDateTime historyChanged;

    public static MemberGradeHistory create(
            Member member,
            MemberGrade newGrade,
            GradeName beforeCode,
            GradeName afterCode
    ) {
        return MemberGradeHistory.builder()
                .member(member)
                .grade(newGrade)
                .historyBefore(beforeCode)
                .historyAfter(afterCode)
                .historyChanged(LocalDateTime.now())
                .build();
    }
}
