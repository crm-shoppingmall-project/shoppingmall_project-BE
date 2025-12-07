package com.twog.shopping.domain.promotion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Segment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "segment_id")
    private Long segmentId;

    @Column(name = "segment_name", nullable = false)
    private String segmentName;

    @Column(name = "segment_rule", columnDefinition = "JSON")
    private String segmentRule; // Simplified as String for JSON
}
