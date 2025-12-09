package com.twog.shopping.domain.member.entity;

import com.twog.shopping.domain.promotion.entity.Segment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "segment_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SegmentMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "segment_member_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "segment_member_joined")
    private LocalDateTime addedAt;

    public static SegmentMember create(Segment segment, Member member) {
        SegmentMember sm = new SegmentMember();
        sm.segment = segment;
        sm.member = member;
        sm.addedAt = LocalDateTime.now();
        return sm;
    }
}
