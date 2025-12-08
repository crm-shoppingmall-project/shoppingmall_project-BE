package com.twog.shopping.domain.purchase.repository;

import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Long> {
}
