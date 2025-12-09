package com.twog.shopping.domain.analytics.scheduler;

import com.twog.shopping.domain.analytics.service.MemberGradeCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class GradeScheduler {

    private final MemberGradeCalculationService memberGradeCalculationService;

    @Scheduled(cron = "0 10 3 1 * *", zone = "Asia/Seoul")
    public void runMonthlyGradeRecalculation() {
        memberGradeCalculationService.recalculateMemberGrades();
    }
}
