package com.twog.shopping.domain.analytics.service;

import com.twog.shopping.domain.analytics.entity.MemberRfm;
import com.twog.shopping.domain.analytics.entity.RfmAggregation;
import com.twog.shopping.domain.analytics.repository.MemberRfmRepository;
import com.twog.shopping.domain.log.repository.HistoryRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RfmCalculationService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final MemberRfmRepository memberRfmRepository;

    /**
     * 오늘 기준으로 R/F/M 값을 계산해서 member_rfm 테이블에 upsert
     */
    @Transactional
    public void calculateRfmForToday() {

        LocalDate today = LocalDate.now();

        // RFM 집계 기간: 일단 최근 1년 기준 (원하면 조정 가능)
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);

        // 1) History에서 R/F/M 집계
        List<RfmAggregation> aggregations = historyRepository.aggregateRfm(start, end);

        for (RfmAggregation agg : aggregations) {

            Long memberId = agg.getMemberId();
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다. memberId=" + memberId));

            // Recency 계산
            LocalDateTime lastPurchaseAt = agg.getLastPurchaseAt();
            int recencyDays = (lastPurchaseAt == null)
                    ? 9999
                    : (int) ChronoUnit.DAYS.between(lastPurchaseAt.toLocalDate(), today);

            int frequency = agg.getFrequency().intValue();
            BigDecimal monetary = agg.getMonetary() != null
                    ? agg.getMonetary()
                    : BigDecimal.ZERO;

            // 2) 기존 MemberRfm 있으면 업데이트, 없으면 새로 생성
            MemberRfm memberRfm = memberRfmRepository.findByMember(member)
                    .orElseGet(() -> MemberRfm.builder()
                            .member(member)
                            .rfmRecencyDays(recencyDays)
                            .rfmFrequency(frequency)
                            .rfmMonetary(monetary)
                            .rfmSnapshot(today)
                            .rfmRScore(0)
                            .rfmFScore(0)
                            .rfmMScore(0)
                            .rfmTotalScore(0)
                            .build()
                    );

            // 값 갱신
            memberRfm.updateRawRfm(recencyDays, frequency, monetary);
            memberRfm.updateSnapshotDate(today);

            memberRfmRepository.save(memberRfm);
        }
    }
}