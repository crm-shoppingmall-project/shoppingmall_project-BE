package com.twog.shopping.domain.cart.repository;

import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.entity.CartItemStatus;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.global.common.entity.GradeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CartItemRepositoryTest {

        @Autowired
        private CartItemRepository cartItemRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private CartRepository cartRepository;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private MemberGradeRepository memberGradeRepository;

        private Member member;
        private Cart cart;
        private Product product1;
        private Product product2;

        @BeforeEach
        void setUp() {
                MemberGrade bronzeGrade = memberGradeRepository.save(MemberGrade.builder()
                                .gradeName(GradeName.BRONZE)
                                .gradeDesc("브론즈 등급")
                                .build());

                // Member 생성 시, 빌더에 누락된 필수 필드를 직접 추가
                member = memberRepository.save(Member.builder()
                                .memberName("testUser")
                                .memberEmail("test@test.com")
                                .memberPwd("password")
                                .memberBirth(LocalDate.of(1990, 1, 1))
                                .memberGender('M')
                                .memberPhone("01012345678")
                                .memberGrade(bronzeGrade)
                                .memberRole(UserRole.USER)
                                .memberStatus(MemberStatus.active)
                                .memberCreated(LocalDateTime.now())
                                .memberUpdated(LocalDateTime.now())
                                .memberLastAt(LocalDateTime.now())
                                .build());

                cart = cartRepository.save(Cart.createCart(member));

                product1 = productRepository.save(Product.builder()
                                .productName("product1")
                                .productCategory("category1")
                                .productPrice(10000)
                                .productQuantity(10)
                                .productStatus(ProductStatus.ACTIVE)
                                .build());

                product2 = productRepository.save(Product.builder()
                                .productName("product2")
                                .productCategory("category2")
                                .productPrice(20000)
                                .productQuantity(20)
                                .productStatus(ProductStatus.ACTIVE)
                                .build());

                CartItem cartItem1 = CartItem.createCartItem(cart, product1, 1);
                cartItemRepository.save(cartItem1);

                CartItem cartItem2 = CartItem.createCartItem(cart, product2, 1);
                cartItem2.updateCartItemStatus(CartItemStatus.REMOVED);
                cartItemRepository.save(cartItem2);
        }

        @Test
        @DisplayName("회원 ID와 상태로 장바구니 아이템 목록을 조회한다")
        void findCartItemsByMemberIdAndStatusTest() {
                // given
                List<CartItemStatus> statuses = Arrays.asList(CartItemStatus.ACTIVE);

                // when
                List<CartItem> cartItems = cartItemRepository
                                .findCartItemsByMemberIdAndStatus(member.getMemberId().intValue(), statuses);

                // then
                assertThat(cartItems).hasSize(1);
                assertThat(cartItems.get(0).getProduct().getProductId()).isEqualTo(product1.getProductId());
                assertThat(cartItems.get(0).getCartItemStatus()).isEqualTo(CartItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("회원 ID와 상태로 장바구니 아이템 페이지를 조회한다")
        void findCartItemsByMemberIdAndStatusPageTest() {
                // given
                List<CartItemStatus> statuses = Arrays.asList(CartItemStatus.ACTIVE);
                Pageable pageable = PageRequest.of(0, 10);

                // when
                Page<CartItem> cartItemPage = cartItemRepository.findCartItemsByMemberIdAndStatusPage(
                                member.getMemberId().intValue(), statuses, pageable);

                // then
                assertThat(cartItemPage.getTotalElements()).isEqualTo(1);
                assertThat(cartItemPage.getContent()).hasSize(1);
                assertThat(cartItemPage.getContent().get(0).getProduct().getProductId())
                                .isEqualTo(product1.getProductId());
                assertThat(cartItemPage.getContent().get(0).getCartItemStatus()).isEqualTo(CartItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("장바구니 ID, 상품 ID, 상태로 장바구니 아이템을 조회한다")
        void findByCart_CartIdAndProduct_ProductIdAndCartItemStatusTest() {
                // when
                Optional<CartItem> foundCartItem = cartItemRepository
                                .findByCart_CartIdAndProduct_ProductIdAndCartItemStatus(
                                                cart.getCartId(), product1.getProductId(), CartItemStatus.ACTIVE);

                // then
                assertThat(foundCartItem).isPresent();
                assertThat(foundCartItem.get().getProduct().getProductId()).isEqualTo(product1.getProductId());
        }

        @Test
        @DisplayName("장바구니 ID, 상품 ID, 상태 목록으로 장바구니 아이템을 조회한다")
        void findByCart_CartIdAndProduct_ProductIdAndCartItemStatusInTest() {
                // given
                List<CartItemStatus> statuses = Arrays.asList(CartItemStatus.ACTIVE, CartItemStatus.UPDATED);

                // when
                Optional<CartItem> foundCartItem = cartItemRepository
                                .findByCart_CartIdAndProduct_ProductIdAndCartItemStatusIn(
                                                cart.getCartId(), product1.getProductId(), statuses);

                // then
                assertThat(foundCartItem).isPresent();
                assertThat(foundCartItem.get().getProduct().getProductId()).isEqualTo(product1.getProductId());
        }
}
