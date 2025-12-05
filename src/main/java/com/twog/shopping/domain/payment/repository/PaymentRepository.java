package com.twog.shopping.domain.payment.repository;

import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPurchaseIdAndType(Long purchase, PaymentType type);

    Optional<Payment> findByPgTid(String pgTid);
}
