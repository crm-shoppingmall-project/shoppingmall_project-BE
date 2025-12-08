package com.twog.shopping.domain.purchase.repository;

import com.twog.shopping.domain.purchase.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
<<<<<<< HEAD
    Page<Purchase> findByMemberId(Long memberId, Pageable pageable);
=======

>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
}