package com.twog.shopping.domain.payment.repository;

import com.twog.shopping.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByPurchase_MemberId(Long memberId, Pageable pageable);
    Optional<Payment> findByPurchase_Id(Long purchaseId);
    Optional<Payment> findByPgTid(String pgTid);
}
