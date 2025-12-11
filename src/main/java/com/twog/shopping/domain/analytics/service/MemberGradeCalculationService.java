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

    // 최소 유지일수
    private static final int GRADE_FREEZE_DAYS = 60;

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

                Member member = memberRepository.getReferenceById(target.getMemberId());

                GradeName beforeGradeCode = target.getCurrentGrade();

                if (isInFreezePeriod(member)) {
                    continue;
                }

                GradeName newGradeCode = decideGradeCodeWithBuffer(totalScore, beforeGradeCode);

                if (beforeGradeCode != null && beforeGradeCode.equals(newGradeCode)) {
                    continue;
                }

                MemberGrade newGrade = memberGradeRepository.findByGradeName(newGradeCode);
                member.changeGrade(newGrade); // 이 안에서 member의 grade, updatedTime 등 갱신

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


    private boolean isInFreezePeriod(Member member) {
        return memberGradeHistoryRepository
                .findTopByMemberOrderByHistoryChangedDesc(member)
                .map(history ->
                        history.getHistoryChanged()
                                .isAfter(java.time.LocalDateTime.now().minusDays(GRADE_FREEZE_DAYS))
                )
                .orElse(false);  // 이력이 없으면 프리즈 기간 없음
    }

    /**
     * 버퍼존이 적용된 등급 산정
     * - currentGrade 가 없으면(신규 회원 등) 기본 구간으로만 판단
     * - 있으면 승급 / 강등 기준을 다르게 적용
     */
    private GradeName decideGradeCodeWithBuffer(int totalScore, GradeName currentGrade) {

        // 1) 현재 등급이 없는 경우: 기본 구간으로만 판단
        if (currentGrade == null) {
            return decideGradeCodeBasic(totalScore);
        }

        switch (currentGrade) {
            case BRONZE:
                // BRONZE에서 내려갈 수는 없음
                if (totalScore >= 7) {
                    return GradeName.SILVER;
                }
                return GradeName.BRONZE;

            case SILVER:
                if (totalScore >= 10) {
                    return GradeName.GOLD;
                }
                if (totalScore < 4) {
                    return GradeName.BRONZE;
                }
                return GradeName.SILVER;     // [4 ~ 9] 버퍼존 → 유지

            case GOLD:
                if (totalScore >= 13) {
                    return GradeName.VIP;
                }
                if (totalScore < 7) {
                    return GradeName.SILVER;
                }
                return GradeName.GOLD;        // [7 ~ 12] 버퍼존 → 유지

            case VIP:
                if (totalScore < 10) {
                    return GradeName.GOLD;    // 강등
                }
                return GradeName.VIP;         // [10 이상] 유지

            default:
                return decideGradeCodeBasic(totalScore);
        }
    }

    /**
     * 버퍼존 고려 없이 점수만으로 등급 나누는 기본 로직
     * (신규회원 등 currentGrade 없는 경우에 사용)
     */
    private GradeName decideGradeCodeBasic(int totalScore) {
        if (totalScore >= 13) return GradeName.VIP;
        if (totalScore >= 10) return GradeName.GOLD;
        if (totalScore >= 7)  return GradeName.SILVER;
        if (totalScore >= 4)  return GradeName.BRONZE;
        return GradeName.BRONZE;
    }



}
