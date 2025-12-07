package com.twog.shopping.domain.analytics.entity;

import com.twog.shopping.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "member_rfm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberRfm {

    @Id
    @Column(name = "rfm_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rfmId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int rfmRecencyDays;

    @Column(nullable = false)
    private int rfmFrequency;

    @Column(nullable = false)
    private BigDecimal rfmMonetary;

    @Column(nullable = false)
    private int rfmRScore;

    @Column(nullable = false)
    private int rfmFScore;

    @Column(nullable = false)
    private int rfmMScore;

    @Column(nullable = false)
    private int rfmTotalScore;

    @Column(nullable = false)
    private LocalDate rfmSnapshot;


    public void updateRawRfm(int recencyDays, int frequency, BigDecimal monetary){
        this.rfmRecencyDays = recencyDays;
        this.rfmFrequency = frequency;
        this.rfmMonetary = monetary;
    }

    public void updateRfmScore(int rScore, int fScore, int mScore){
        this.rfmRScore = rScore;
        this.rfmFScore = fScore;
        this.rfmMScore = mScore;
    }


    public void updateSnapshotDate(LocalDate snapshotDate){
        this.rfmSnapshot = snapshotDate;
    }






}
