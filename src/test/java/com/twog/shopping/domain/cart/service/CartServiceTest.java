package com.twog.shopping.domain.cart.service;

import com.twog.shopping.domain.cart.dto.CartDetailDto;
import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.*;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.product.entity.Product;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private MemberGradeRepository memberGradeRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long existingMemberId;
    private Long newMemberId;
    private Integer productId1;
    private Integer productId2;

    @BeforeEach
    void setUp() {
        MemberGrade bronzeGrade = memberGradeRepository.save(MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build());

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
                .memberCreated(LocalDateTime.now())
                .memberUpdated(LocalDateTime.now())
                .memberLastAt(LocalDateTime.now())
                .build();
        Member existingMember = memberRepository.save(member1);
        existingMemberId = existingMember.getMemberId();

        Cart cart = Cart.createCart(existingMember);
        cartRepository.save(cart);

        Member member2 = Member.builder()
                .memberName("newUser")
                .memberEmail("new@test.com")
                .memberPwd("password")
                .memberBirth(LocalDate.of(1995, 2, 2))
                .memberGender('F')
                .memberPhone("010-2222-2222")
                .memberGrade(bronzeGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .memberCreated(LocalDateTime.now())
                .memberUpdated(LocalDateTime.now())
                .memberLastAt(LocalDateTime.now())
                .build();
        Member newMember = memberRepository.save(member2);
        newMemberId = newMember.getMemberId();

        Product product1 = productRepository.save(Product.builder()
                .productName("Test Product 1")
                .productPrice(10000)
                .productQuantity(100)
                .productCategory("cat1")
                .productStatus(com.twog.shopping.domain.product.entity.ProductStatus.ACTIVE)
                .build());
        productId1 = product1.getProductId();

        Product product2 = productRepository.save(Product.builder()
                .productName("Test Product 2")
                .productPrice(20000)
                .productQuantity(50)
                .productCategory("cat2")
                .productStatus(com.twog.shopping.domain.product.entity.ProductStatus.ACTIVE)
                .build());
        productId2 = product2.getProductId();
    }

    @Test
    @DisplayName("기존 장바구니가 있는 회원은 해당 장바구니를 조회한다")
    void testFindOrCreateCart_ExistingCart() {
        // when
        Cart foundCart = cartService.findOrCreateCartByMemberId(existingMemberId.intValue());

        // then
        assertNotNull(foundCart);
        assertEquals(existingMemberId, foundCart.getMember().getMemberId());
    }

    @Test
    @DisplayName("장바구니가 없는 새로운 회원은 새 장바구니를 생성하여 반환한다")
    void testFindOrCreateCart_NewCart() {
        // given
        assertFalse(cartRepository.findByMember_MemberId(newMemberId.intValue()).isPresent());

        // when
        Cart createdCart = cartService.findOrCreateCartByMemberId(newMemberId.intValue());

        // then
        assertNotNull(createdCart);
        assertNotNull(createdCart.getCartId()); // int는 null이 될 수 없지만, ID 생성 여부 확인을 위해 유지
        assertEquals(newMemberId, createdCart.getMember().getMemberId());
        assertTrue(cartRepository.findByMember_MemberId(newMemberId.intValue()).isPresent());
    }

    @Test
    @DisplayName("장바구니가 있는 회원의 장바구니와 연관 데이터를 전체 삭제한다")
    void testDeleteCart_ExistingCart() {
        // given
        assertTrue(cartRepository.findByMember_MemberId(existingMemberId.intValue()).isPresent());

        // when
        cartService.deleteCartByMemberId(existingMemberId.intValue());

        // then
        assertFalse(cartRepository.findByMember_MemberId(existingMemberId.intValue()).isPresent());
    }

    @Test
    @DisplayName("장바구니가 없는 회원의 삭제 요청 시 아무일도 일어나지 않는다")
    void testDeleteCart_NonExistingCart() {
        // given
        assertFalse(cartRepository.findByMember_MemberId(newMemberId.intValue()).isPresent());

        // when & then
        assertDoesNotThrow(() -> cartService.deleteCartByMemberId(newMemberId.intValue()));
    }

    @Test
    @DisplayName("회원 ID로 장바구니 상품 목록을 조회한다")
    void testGetCartItemsByMemberId() {
        // given
        cartService.addItemToCart(existingMemberId.intValue(), productId1, 2);

        // when
        List<CartItem> cartItems = cartService.getCartItemsByMemberId(existingMemberId.intValue());

        // then
        assertNotNull(cartItems);
        assertThat(cartItems).hasSize(1);
    }

    @Test
    @DisplayName("장바구니에 상품을 추가한다")
    void addItemToCartTest() {
        // when
        cartService.addItemToCart(existingMemberId.intValue(), productId1, 2);

        // then
        Cart cart = cartService.findOrCreateCartByMemberId(existingMemberId.intValue());
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems().getFirst().getProduct().getProductId()).isEqualTo(productId1);
        assertThat(cart.getCartItems().getFirst().getCartItemQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니에서 상품을 제거한다 (상태 변경)")
    void removeItemFromCartTest() {
        // given
        cartService.addItemToCart(existingMemberId.intValue(), productId1, 1);

        // when
        cartService.removeItemFromCart(existingMemberId.intValue(), productId1);

        // then
        // removeItem은 상태를 REMOVED로 변경하므로, 활성 아이템 목록은 비어있어야 함
        List<CartItem> activeItems = cartService.getActiveCartItemsByMemberId(existingMemberId.intValue());
        assertThat(activeItems).isEmpty();
    }

    @Test
    @DisplayName("장바구니 상품의 수량을 변경한다")
    void updateCartItemQuantityTest() {
        // given
        cartService.addItemToCart(existingMemberId.intValue(), productId1, 1);

        // when
        cartService.updateCartItemQuantity(existingMemberId.intValue(), productId1, 5);

        // then
        Cart cart = cartService.findOrCreateCartByMemberId(existingMemberId.intValue());
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems().getFirst().getCartItemQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 상세 정보를 조회한다")
    void getCartDetailsTest() {
        // given
        cartService.addItemToCart(existingMemberId.intValue(), productId1, 3);
        cartService.addItemToCart(existingMemberId.intValue(), productId2, 1);

        // when
        List<CartDetailDto> cartDetails = cartService.getCartDetails(existingMemberId.intValue());

        // then
        assertThat(cartDetails).hasSize(2);
        Optional<CartDetailDto> detail1 = cartDetails.stream().filter(d -> d.getProductId() == productId1).findFirst();
        assertThat(detail1).isPresent();
        assertThat(detail1.get().getCartQuantity()).isEqualTo(3);
        assertThat(detail1.get().isOrderable()).isTrue();
    }

    @Test
    @DisplayName("장바구니 상품들을 일괄 업데이트한다")
    void updateCartItemsTest() {
        // given
        cartService.addItemToCart(existingMemberId.intValue(), productId1, 1);
        cartService.addItemToCart(existingMemberId.intValue(), productId2, 1);

        List<CartDetailDto> dtosToUpdate = new ArrayList<>();
        dtosToUpdate.add(CartDetailDto.builder().productId(productId1).cartQuantity(10).build());
        dtosToUpdate.add(CartDetailDto.builder().productId(productId2).cartQuantity(20).build());

        // when
        cartService.updateCartItems(existingMemberId.intValue(), dtosToUpdate);

        // then
        List<CartDetailDto> updatedDetails = cartService.getCartDetails(existingMemberId.intValue());
        Optional<CartDetailDto> detail1 = updatedDetails.stream().filter(d -> d.getProductId() == productId1).findFirst();
        assertThat(detail1).isPresent();
        assertThat(detail1.get().getCartQuantity()).isEqualTo(10);

        Optional<CartDetailDto> detail2 = updatedDetails.stream().filter(d -> d.getProductId() == productId2).findFirst();
        assertThat(detail2).isPresent();
        assertThat(detail2.get().getCartQuantity()).isEqualTo(20);
    }
}
