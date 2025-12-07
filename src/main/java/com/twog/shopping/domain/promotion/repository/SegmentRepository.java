package com.twog.shopping.domain.promotion.repository;

import com.twog.shopping.domain.promotion.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {
}
