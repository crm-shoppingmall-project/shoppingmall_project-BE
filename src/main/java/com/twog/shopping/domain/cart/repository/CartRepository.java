package com.twog.shopping.domain.cart.repository;

import com.twog.shopping.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    Optional<Cart> findByMember_MemberId(int memberId);
}
