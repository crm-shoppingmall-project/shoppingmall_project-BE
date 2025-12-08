package com.twog.shopping.domain.purchase.repository;

import com.twog.shopping.domain.purchase.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
}