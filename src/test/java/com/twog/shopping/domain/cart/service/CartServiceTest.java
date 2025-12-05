package com.twog.shopping.domain.cart.service;

import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.*;
import com.twog.shopping.domain.member.repository.MemberGradeRepository; // MemberGradeRepository import
import com.twog.shopping.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberGradeRepository memberGradeRepository; // MemberGradeRepository 주입

    private int existingMemberId; // 장바구니가 있는 회원 ID
    private int newMemberId;      // 장바구니가 없는 회원 ID

    @BeforeEach
    void setUp() {
        // 1. MemberGrade 객체를 먼저 생성하고 저장
        MemberGrade bronzeGrade = MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build();
        memberGradeRepository.save(bronzeGrade);

        // 2. 기존 회원 및 장바구니 생성
        Member member1 = Member.builder()
                .memberName("existingUser")
                .memberEmail("existing@test.com")
                .memberPwd("password")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberGender('M')
                .memberPhone("010-1111-1111")
                .memberGrade(bronzeGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .build();
        Member existingMember = memberRepository.save(member1);
        existingMemberId = existingMember.getMemberId().intValue();

        Cart cart = new Cart();
        cart.setMember(existingMember);
        cartRepository.save(cart);

        // 3. 신규 회원 생성
        Member member2 = Member.builder()
                .memberName("newUser")
                .memberEmail("new@test.com")
                .memberPwd("password")
                .memberBirth(LocalDate.of(1995, 2, 2))
                .memberGender('F')
                .memberPhone("010-2222-2222")
                .memberGrade(bronzeGrade)
                .memberRole(UserRole.ADMIN)
                .memberStatus(MemberStatus.active)
                .build();
        Member newMember = memberRepository.save(member2);
        newMemberId = newMember.getMemberId().intValue();
    }

    @Test
    @DisplayName("기존 장바구니가 있는 회원은 해당 장바구니를 조회한다")
    void testFindOrCreateCart_ExistingCart() {
        // when
        Cart foundCart = cartService.findOrCreateCartByMemberId(existingMemberId);

        // then
        assertNotNull(foundCart);
        assertEquals(existingMemberId, foundCart.getMember().getMemberId().intValue());
    }

    @Test
    @DisplayName("장바구니가 없는 새로운 회원은 새 장바구니를 생성하여 반환한다")
    void testFindOrCreateCart_NewCart() {
        // given
        assertFalse(cartRepository.findByMember_MemberId(newMemberId).isPresent());

        // when
        Cart createdCart = cartService.findOrCreateCartByMemberId(newMemberId);

        // then
        assertNotNull(createdCart);
        assertNotNull(createdCart.getCartId());
        assertEquals(newMemberId, createdCart.getMember().getMemberId().intValue());
        assertTrue(cartRepository.findByMember_MemberId(newMemberId).isPresent());
    }

    @Test
    @DisplayName("장바구니가 있는 회원의 장바구니와 연관 데이터를 전체 삭제한다")
    void testDeleteCart_ExistingCart() {
        // given
        assertTrue(cartRepository.findByMember_MemberId(existingMemberId).isPresent());

        // when
        cartService.deleteCartByMemberId(existingMemberId);

        // then
        assertFalse(cartRepository.findByMember_MemberId(existingMemberId).isPresent());
    }

    @Test
    @DisplayName("장바구니가 없는 회원의 삭제 요청 시 아무일도 일어나지 않는다")
    void testDeleteCart_NonExistingCart() {
        // given
        assertFalse(cartRepository.findByMember_MemberId(newMemberId).isPresent());

        // when & then
        assertDoesNotThrow(() -> cartService.deleteCartByMemberId(newMemberId));
    }

    @Test
    @DisplayName("회원 ID로 장바구니 상품 목록을 조회한다")
    void testGetCartItemsByMemberId() {
        // when
        List<CartItem> cartItems = cartService.getCartItemsByMemberId(existingMemberId);

        // then
        assertNotNull(cartItems);
    }
}
