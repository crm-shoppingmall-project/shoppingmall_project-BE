package com.twog.shopping.domain.promotion.repository;

import com.twog.shopping.domain.promotion.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
}
