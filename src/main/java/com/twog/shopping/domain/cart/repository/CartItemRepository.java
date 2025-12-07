package com.twog.shopping.domain.cart.repository;

import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.entity.CartItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c JOIN FETCH ci.product p WHERE c.member.memberId = :memberId AND ci.cartItemStatus IN :statuses")
    List<CartItem> findCartItemsByMemberIdAndStatus(@Param("memberId") int memberId,
            @Param("statuses") List<CartItemStatus> statuses);

    Optional<CartItem> findByCart_CartIdAndProduct_ProductIdAndCartItemStatus(int cartId, int productId,
            CartItemStatus status);

    Optional<CartItem> findByCart_CartIdAndProduct_ProductIdAndCartItemStatusIn(int cartId, int productId,
            List<CartItemStatus> statuses);
}
