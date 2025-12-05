package com.twog.shopping.domain.payment.service;

import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PurchaseService purchaseService;

    @Transactional
    public void approePayment(Payment payment) {

    }
}
