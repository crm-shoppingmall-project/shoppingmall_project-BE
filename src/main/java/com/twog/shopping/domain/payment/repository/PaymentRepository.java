package com.twog.shopping.domain.payment.repository;

import com.twog.shopping.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
