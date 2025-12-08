package com.twog.shopping.domain.analytics.service;

import com.twog.shopping.domain.analytics.entity.MemberGradeHistory;
import com.twog.shopping.domain.analytics.entity.MemberGradeTarget;
import com.twog.shopping.domain.analytics.repository.MemberGradeHistoryRepository;
import com.twog.shopping.domain.analytics.repository.MemberRfmRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.global.common.entity.GradeName;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
@RequiredArgsConstructor
public class MemberGradeCalculationService {

    private final MemberRfmRepository memberRfmRepository;
    private final MemberGradeRepository memberGradeRepository;
    private final MemberGradeHistoryRepository memberGradeHistoryRepository;

    private static final int PAGE_SIZE = 100;
    private final MemberRepository memberRepository;

    /**
     * RFM totalScore 기준으로 전체 회원 등급 산정 + 이력 기록
     */
    @Transactional
    public void recalculateMemberGrades() {

        int page = 0;
        boolean hasNext = true;

        while (hasNext) {
            var pageable = PageRequest.of(page,PAGE_SIZE);
            var targets = memberRfmRepository.findGradeTargets(pageable);

            if(targets.isEmpty()) break;


            for (MemberGradeTarget target : targets) {

                int totalScore = target.getRfmTotalScore();
                GradeName newGradeCode = decideGradeCode(totalScore);
                GradeName beforeGradeCode = target.getCurrentGrade();

                if (beforeGradeCode != null && beforeGradeCode.equals(newGradeCode)) {
                    continue;
                }

                MemberGrade newGrade = memberGradeRepository.findByGradeName(newGradeCode);

                Member member = memberRepository.getReferenceById(target.getMemberId());
                member.changeGrade(newGrade);

                // 3) 등급 변경 이력 기록
                MemberGradeHistory history = MemberGradeHistory.create(
                        member,
                        newGrade,
                        beforeGradeCode,
                        newGradeCode
                );

                memberGradeHistoryRepository.save(history);
            }


            hasNext = targets.hasNext();
            page ++;
        }

    }

    private GradeName decideGradeCode(int totalScore) {
        if (totalScore >= 13) return GradeName.VIP;
        if (totalScore >= 10) return GradeName.GOLD;
        if (totalScore >= 7) return GradeName.SILVER;
        if (totalScore >= 4) return GradeName.BRONZE;
        return GradeName.BRONZE;
    }

    private GradeName getCurrentGradeCode(Member member) {
        if (member.getMemberGrade() == null) {
            return null;
        }
        return member.getMemberGrade().getGradeName();
    }
}
