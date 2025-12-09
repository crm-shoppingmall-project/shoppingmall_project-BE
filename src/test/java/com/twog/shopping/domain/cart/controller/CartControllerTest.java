package com.twog.shopping.domain.cart.controller;

import com.twog.shopping.domain.cart.dto.CartDetailDto;
import com.twog.shopping.domain.cart.dto.CartItemRequestDto;
import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import com.twog.shopping.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CartControllerTest {

    @Autowired
    private CartController cartController;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    private int testProductId;

    @BeforeEach
    void setUp() {
        // [설정] 통합 테스트 환경 구축
        // 1. 테스트 실행을 위한 기초 데이터(회원 등급, 회원, 상품)를 DB에 미리 생성합니다.
        // @Transactional 어노테이션으로 인해 테스트 종료 시 데이터는 자동 롤백됩니다.
        // (Native Query를 사용해 ID를 1로 고정하여 생성 - 테스트의 일관성을 위함)

        entityManager.createNativeQuery(
                "INSERT INTO member_grade (grade_code, grade_name, grade_desc) " +
                        "SELECT 1, 'BRONZE', '일반 회원' " +
                        "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member_grade WHERE grade_code = 1)")
                .executeUpdate();

        entityManager.createNativeQuery(
                "INSERT INTO member (member_id, grade_code, member_name, member_gender, member_phone, member_birth, member_pwd, member_email, member_status, member_created, member_updated, member_last_at, member_role) "
                        +
                        "SELECT 1, 1, '테스터', 'M', '010-1234-5678', '1990-01-01', 'password', 'test@test.com', 'active', NOW(), NOW(), NOW(), 'USER' "
                        +
                        "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_id = 1)")
                .executeUpdate();

        // 2. 테스트용 상품 생성 (어떤 ID여도 상관없음)
        Product product = Product.builder()
                .productName("테스트 상품")
                .productCategory("카테고리")
                .productPrice(10000)
                .productQuantity(100)
                .productStatus(ProductStatus.ACTIVE)
                .build();
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getProductId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("장바구니 상품 추가 통합 테스트 - 컨트롤러 직접 호출")
    void addItemToCart_Integration() {
        // [시나리오]
        // 1. 로그인한 사용자가 특정 상품을 5개 장바구니에 담는다.
        // 2. 요청이 성공(200 OK)하는지 검증한다.
        // 3. 실제 DB에 해당 상품이 올바른 수량(5개)으로 저장되었는지 확인한다.

        // given

        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(5);

        Member member = memberRepository.findById(1L).orElseThrow();
        DetailsUser user = new DetailsUser(member);

        // when
        ResponseEntity<String> response = cartController.addItemToCart(user, requestDto);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("장바구니에 상품이 추가되었습니다.");

        // DB 검증
        Cart cart = cartRepository.findByMember_MemberId(1).orElseThrow();
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems().get(0).getCartItemQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 상세 조회 통합 테스트 - 컨트롤러 직접 호출")
    void getCartDetails_Integration() {
        // [시나리오]
        // 1. 사용자가 상품(ID: testProductId)을 2개 장바구니에 미리 담아둔다.
        // 2. 장바구니 상세 조회 API를 호출한다.
        // 3. 응답 결과에 해당 상품 정보(이름, 수량 2개)가 정확히 포함되어 있는지 검증한다.

        // given: 장바구니에 상품을 미리 담아둠 (Controller 메서드 재활용 가능)
        Member member = memberRepository.findById(1L).orElseThrow();
        DetailsUser user = new DetailsUser(member);

        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(2);
        cartController.addItemToCart(user, requestDto);

        entityManager.flush();
        entityManager.clear();

        // when
        // 다시 로드해서 영속성 컨텍스트 관리 (테스트 트랜잭션 내이므로)
        member = memberRepository.findById(1L).orElseThrow();
        user = new DetailsUser(member);
        ResponseEntity<List<CartDetailDto>> response = cartController.getCartDetails(user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<CartDetailDto> details = response.getBody();
        assertThat(details).isNotNull();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getProductName()).isEqualTo("테스트 상품");
        assertThat(details.get(0).getCartQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경 통합 테스트 - 컨트롤러 직접 호출")
    void updateCartItemQuantity_Integration() {
        // [시나리오]
        // 1. 사용자가 장바구니에 상품을 3개 담아둔 상태이다.
        // 2. 해당 상품의 수량을 10개로 변경 요청한다.
        // 3. 변경 요청이 성공하는지 확인하고, DB에 실제 수량이 10개로 반영되었는지 검증한다.

        // given
        Member member = memberRepository.findById(1L).orElseThrow();
        DetailsUser user = new DetailsUser(member);

        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setProductId(testProductId);
        requestDto.setQuantity(3);
        cartController.addItemToCart(user, requestDto); // 3개 담기

        // when
        requestDto.setQuantity(10); // 10개로 변경 요청
        ResponseEntity<String> response = cartController.updateCartItemQuantity(user, requestDto);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("장바구니 상품 수량이 업데이트되었습니다.");

        // DB 검증
        entityManager.flush();
        entityManager.clear();

        Cart cart = cartRepository.findByMember_MemberId(1).orElseThrow();
        assertThat(cart.getCartItems().get(0).getCartItemQuantity()).isEqualTo(10);
    }
}
