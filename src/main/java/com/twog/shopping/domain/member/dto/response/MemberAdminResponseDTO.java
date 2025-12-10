package com.twog.shopping.domain.member.dto.response;

import com.twog.shopping.domain.analytics.entity.MemberGradeHistory;
import com.twog.shopping.domain.analytics.entity.MemberRfm;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.global.common.entity.GradeName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberAdminResponseDTO {
    private Long memberId;
    private String memberEmail;
    private String memberName;
    private MemberStatus memberStatus;
    private GradeName gradeName;
    private Integer rfmTotalScore;
    private LocalDateTime gradeChangedAt;

    public static MemberAdminResponseDTO fromEntity(Member member, MemberRfm rfm, MemberGradeHistory gradeHistory) {
        return MemberAdminResponseDTO.builder()
                .memberId(member.getMemberId())
                .memberEmail(member.getMemberEmail())
                .memberName(member.getMemberName())
                .memberStatus(member.getMemberStatus())
                .gradeName(member.getMemberGrade().getGradeName())
                .rfmTotalScore(rfm != null ? rfm.getRfmTotalScore() : null)
                .gradeChangedAt(gradeHistory != null ? gradeHistory.getHistoryChanged() : null)
                .build();
    }
}
