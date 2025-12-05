package com.twog.shopping.domain.cart.repository;

import com.twog.shopping.domain.cart.entity.Cart;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberGradeRepository memberGradeRepository; // MemberGradeRepository 주입

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 1. MemberGrade 객체를 먼저 생성하고 저장
        MemberGrade bronzeGrade = MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build();
        memberGradeRepository.save(bronzeGrade);

        // 2. 빌더 패턴을 사용하여 Member 객체 생성 시, 저장된 MemberGrade 객체를 설정
        Member member = Member.builder()
                .memberName("testUser")
                .memberEmail("test@test.com")
                .memberPwd("password")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberGender('M')
                .memberPhone("010-1234-5678")
                .memberGrade(bronzeGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .memberGrade(bronzeGrade) // memberGrade 설정 추가
                .build();
        
        testMember = memberRepository.save(member);
    }

    @Test
    @DisplayName("특정 회원의 ID로 장바구니를 조회한다")
    void testFindByMember_MemberId() {
        // given
        Cart cart = new Cart();
        cart.setMember(testMember);
        cartRepository.save(cart);

        // when
        Optional<Cart> foundCart = cartRepository.findByMember_MemberId(testMember.getMemberId().intValue());

        // then
        assertTrue(foundCart.isPresent());
        assertEquals(testMember.getMemberId(), foundCart.get().getMember().getMemberId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 장바구니 조회 시 비어있는 Optional을 반환한다")
    void testFindByMember_MemberId_NotFound() {
        // given
        int nonExistentMemberId = 999;

        // when
        Optional<Cart> foundCart = cartRepository.findByMember_MemberId(nonExistentMemberId);

        // then
        assertFalse(foundCart.isPresent());
    }
}
