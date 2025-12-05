package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PurchaseServiceTest {

    @Autowired
    private  PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 NoSuchElementException을 발생시켜야 한다.")
    void findById_NotFound_Failure() {
        // Given
        Long nonExistentId = 9999L;

        // When Then
        assertThrows(NoSuchElementException.class, () -> {
            purchaseService.findById(nonExistentId);
        }, "주문 ID를 찾을 수 없습니다.");
    }
}
