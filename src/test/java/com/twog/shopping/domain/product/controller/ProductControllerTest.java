package com.twog.shopping.domain.product.controller;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.product.dto.ProductRequestDto;
import com.twog.shopping.domain.product.dto.ProductResponseDto;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductControllerTest {

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Long adminMemberId;
    private Long userMemberId;
    private int product1Id;

    @BeforeEach
    void setUp() {
        // 1. 등급 데이터 생성 (존재하지 않으면)
        entityManager.createNativeQuery(
                        "INSERT INTO member_grade (grade_name, grade_desc) " +
                                "SELECT 'BRONZE', '브론즈' FROM DUAL " +
                                "WHERE NOT EXISTS (SELECT 1 FROM member_grade WHERE grade_name = 'BRONZE')")
                .executeUpdate();

        // 2. ADMIN 회원 생성 (ID: 1)
        entityManager.createNativeQuery(
                        "INSERT INTO member (member_id, grade_code, member_name, member_gender, member_phone, member_birth, member_pwd, member_email, member_status, member_created, member_updated, member_last_at, member_role) "
                                +
                                "SELECT 1, (SELECT grade_code FROM member_grade WHERE grade_name = 'BRONZE' LIMIT 1), 'admin', 'M', '01000000000', '1990-01-01', 'password', 'admin@test.com', 'active', NOW(), NOW(), NOW(), 'ADMIN' "
                                +
                                "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_id = 1)")
                .executeUpdate();
        adminMemberId = 1L;

        // 3. USER 회원 생성 (ID: 2)
        entityManager.createNativeQuery(
                        "INSERT INTO member (member_id, grade_code, member_name, member_gender, member_phone, member_birth, member_pwd, member_email, member_status, member_created, member_updated, member_last_at, member_role) "
                                +
                                "SELECT 2, (SELECT grade_code FROM member_grade WHERE grade_name = 'BRONZE' LIMIT 1), 'user', 'M', '01011112222', '1990-01-01', 'password', 'user@test.com', 'active', NOW(), NOW(), NOW(), 'USER' "
                                +
                                "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_id = 2)")
                .executeUpdate();
        userMemberId = 2L;

        // 4. 테스트 상품 생성 (Product1)
        Product product1 = productRepository.save(Product.builder()
                .productName("Test Product 1")
                .productCategory("Category A")
                .productPrice(10000)
                .productQuantity(100)
                .productStatus(ProductStatus.ACTIVE)
                .build());
        product1Id = product1.getProductId();

        entityManager.flush();
        entityManager.clear();
    }

    // Helper: SecurityContext에 인증 정보 설정
    private void mockLogin(Long memberId) {
        Member member = entityManager.find(Member.class, memberId);
        DetailsUser detailsUser = new DetailsUser(member);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                detailsUser, null, detailsUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("ADMIN 권한으로 상품을 성공적으로 생성한다")
    void createProduct_AsAdmin() {
        // given
        mockLogin(adminMemberId); // ADMIN 로그인
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .productName("New Product")
                .productCategory("Category C")
                .productPrice(15000)
                .productQuantity(200)
                .productStatus(ProductStatus.ACTIVE)
                .build();

        // when
        ResponseEntity<ProductResponseDto> response = productController.createProduct(requestDto);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductName()).isEqualTo("New Product");
    }

    @Test
    @DisplayName("USER 권한으로 상품 생성 시 AccessDeniedException이 발생한다")
    void createProduct_AsUser_Forbidden() {
        // given
        mockLogin(userMemberId); // USER 로그인
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .productName("Forbidden Product")
                .build();

        // when & then
        assertThatThrownBy(() -> productController.createProduct(requestDto))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ADMIN 권한으로 상품을 성공적으로 삭제한다")
    void deleteProduct_AsAdmin() {
        // given
        mockLogin(adminMemberId); // ADMIN 로그인

        // when
        ResponseEntity<String> response = productController.deleteProduct(product1Id);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // DB 검증
        Product deletedProduct = productRepository.findById(product1Id).orElseThrow();
        assertThat(deletedProduct.getProductStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("USER 권한으로 상품 삭제 시 AccessDeniedException이 발생한다")
    void deleteProduct_AsUser_Forbidden() {
        // given
        mockLogin(userMemberId); // USER 로그인

        // when & then
        assertThatThrownBy(() -> productController.deleteProduct(product1Id))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("USER 권한으로 전체 상품 조회 시 DELETED 상품은 제외된다")
    void findProducts_AsUser_FilterDeleted() {
        // given: Product1을 삭제 상태로 변경
        mockLogin(adminMemberId);
        productController.deleteProduct(product1Id); // 관리자 권한으로 삭제
        entityManager.flush();
        entityManager.clear();

        // when: USER 로그인 후 조회
        Member userMember = entityManager.find(Member.class, userMemberId);
        DetailsUser detailsUser = new DetailsUser(userMember);

        // product1Id로 직접 조회 시도
        ResponseEntity<List<ProductResponseDto>> response = productController.getProducts(product1Id, null,
                null, detailsUser);

        // then
        List<ProductResponseDto> products = response.getBody();
        assertThat(products).isEmpty(); // 삭제된 상품이므로 조회되지 않아야 함
    }

    @Test
    @DisplayName("ADMIN 권한으로 전체 상품 조회 시 DELETED 상품도 포함된다")
    void findProducts_AsAdmin_IncludeDeleted() {
        // given: Product1을 삭제 상태로 변경
        mockLogin(adminMemberId);
        productController.deleteProduct(product1Id);
        entityManager.flush();
        entityManager.clear();

        // when: ADMIN 로그인 후 조회
        Member adminMember = entityManager.find(Member.class, adminMemberId);
        DetailsUser detailsAdmin = new DetailsUser(adminMember);

        // product1Id로 직접 조회
        ResponseEntity<List<ProductResponseDto>> response = productController.getProducts(product1Id, null,
                null, detailsAdmin);

        // then
        List<ProductResponseDto> products = response.getBody();
        assertThat(products).isNotEmpty();
        assertThat(products.get(0).getProductId()).isEqualTo(product1Id);
        assertThat(products.get(0).getProductStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("USER 권한으로 상품 조회 시 등급에 따른 할인이 적용된다")
    void verifyDiscount_AsUser() {
        // given
        // USER 회원의 등급을 BRONZE 등급으로 설정
        entityManager.createNativeQuery(
                        "UPDATE member SET grade_code = (SELECT grade_code FROM member_grade WHERE grade_name = 'BRONZE' LIMIT 1) WHERE member_id = :memberId")
                .setParameter("memberId", userMemberId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        mockLogin(userMemberId); // USER 로그인 (BRONZE)

        // when
        Member userMember = entityManager.find(Member.class, userMemberId);
        DetailsUser detailsUser = new DetailsUser(userMember);

        ResponseEntity<List<ProductResponseDto>> response = productController.getProducts(product1Id, null,
                null, detailsUser);

        // then
        List<ProductResponseDto> products = response.getBody();
        assertThat(products).isNotEmpty();
        ProductResponseDto product = products.get(0);

        // BRONZE 등급 (2%) 할인 적용 확인
        // 10000 -> 9800
        assertThat(product.getProductPrice()).isEqualTo(10000);
        assertThat(product.getDiscountPrice()).isEqualTo(9800);
    }

    @Test
    @DisplayName("상품 목록 페이징 조회 통합 테스트")
    void getProductsPage_Integration() {
        // given
        // 추가 상품 생성
        for (int i = 0; i < 5; i++) {
            productRepository.save(Product.builder()
                    .productName("Paged Product " + i)
                    .productCategory("Category P")
                    .productPrice(1000 + i)
                    .productQuantity(10)
                    .productStatus(ProductStatus.ACTIVE)
                    .build());
        }

        mockLogin(userMemberId); // USER 로그인
        Member userMember = entityManager.find(Member.class, userMemberId);
        DetailsUser detailsUser = new DetailsUser(userMember);

        PageRequest pageable = PageRequest.of(0, 3); // 3개씩 조회

        // when
        ResponseEntity<Page<ProductResponseDto>> response = productController.getProductsPage(null, null,
                "Category P",
                pageable, detailsUser);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<ProductResponseDto> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent().get(0).getProductCategory()).isEqualTo("Category P");
    }
}
