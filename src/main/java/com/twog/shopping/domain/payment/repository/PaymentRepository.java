package com.twog.shopping.domain.payment.repository;

import com.twog.shopping.domain.payment.entity.Payment;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
import com.twog.shopping.domain.payment.entity.PaymentType;
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

<<<<<<< HEAD
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByPurchase_MemberId(Long memberId, Pageable pageable);
    Optional<Payment> findByPurchase_Id(Long purchaseId);
=======
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPurchaseIdAndType(Long purchase, PaymentType type);

    Optional<Payment> findByPgTid(String pgTid);
>>>>>>> f6f9da05428190d585720de9df0ed89afa7bba66
}
