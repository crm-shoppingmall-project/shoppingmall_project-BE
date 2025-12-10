package com.twog.shopping.domain.product.service;

import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.product.dto.ProductRequestDto;
import com.twog.shopping.domain.product.dto.ProductResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
// 각 테스트 메서드가 끝날 때마다 컨텍스트를 리셋하도록 만드는 방법(차후 독립적 테스트가 필요할 경우 유용)
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = productRepository.save(Product.builder()
                .productName("Test Product 1")
                .productCategory("Category A")
                .productPrice(10000)
                .productQuantity(100)
                .productStatus(ProductStatus.ACTIVE)
                .build());

        productRepository.save(Product.builder()
                .productName("Test Product 2")
                .productCategory("Category B")
                .productPrice(20000)
                .productQuantity(50)
                .productStatus(ProductStatus.INACTIVE)
                .build());
    }

    @Test
    @DisplayName("상품을 성공적으로 생성한다")
    void createProductTest() {
        // given
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .productName("New Product")
                .productCategory("Category C")
                .productPrice(15000)
                .productQuantity(200)
                .productStatus(ProductStatus.ACTIVE)
                .build();

        // when
        ProductResponseDto responseDto = productService.createProduct(requestDto);

        // then
        assertThat(responseDto.getProductName()).isEqualTo("New Product");
        assertThat(responseDto.getProductPrice()).isEqualTo(15000);
        assertThat(productRepository.findById(responseDto.getProductId())).isPresent();
    }

    @Test
    @DisplayName("상품 정보를 성공적으로 수정한다")
    void updateProductTest() {
        // given
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .productName("Updated Product 1")
                .productCategory("Category A-1")
                .productPrice(12000)
                .productQuantity(150)
                .productStatus(ProductStatus.INACTIVE)
                .build();

        // when
        ProductResponseDto responseDto = productService.updateProduct(product1.getProductId(), requestDto);

        // then
        assertThat(responseDto.getProductName()).isEqualTo("Updated Product 1");
        assertThat(responseDto.getProductPrice()).isEqualTo(12000);
        assertThat(responseDto.getProductStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정 시 예외를 발생시킨다")
    void updateNonExistingProductTest() {
        // given
        int nonExistingProductId = 999;
        ProductRequestDto requestDto = ProductRequestDto.builder().build();

        // when & then
        assertThrows(RuntimeException.class, () -> productService.updateProduct(nonExistingProductId, requestDto));
    }

    @Test
    @DisplayName("상품을 논리적으로 삭제한다")
    void deleteProductTest() {
        // when
        productService.deleteProduct(product1.getProductId());

        // then
        Optional<Product> deletedProductOpt = productRepository.findById(product1.getProductId());
        assertThat(deletedProductOpt).isPresent();
        assertThat(deletedProductOpt.get().getProductStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("모든 조건 없이 상품 목록을 조회한다")
    void findProductsWithoutConditionsTest() {
        // when
        List<ProductResponseDto> products = productService.findProducts(null, null, null, null, null);

        // then
        assertThat(products).extracting("productName")
                .contains(product1.getProductName(), "Test Product 2");
    }

    @Test
    @DisplayName("상품 이름으로 상품 목록을 조회한다")
    void findProductsByProductNameTest() {
        // when
        List<ProductResponseDto> products = productService.findProducts(null, "Product 1", null, null, null);

        // then
        assertThat(products).hasSize(1);
        assertThat(products.getFirst().getProductName()).isEqualTo("Test Product 1");
    }

    @Test
    @DisplayName("카테고리로 상품 목록을 조회한다")
    void findProductsByCategoryTest() {
        // when
        List<ProductResponseDto> products = productService.findProducts(null, null, "Category B", null, null);

        // then
        assertThat(products).hasSize(1);
        assertThat(products.getFirst().getProductName()).isEqualTo("Test Product 2");
    }

    @Test
    @DisplayName("관리자는 삭제된 상품도 조회할 수 있다")
    void findProductsByAdminTest() {
        // given
        productService.deleteProduct(product1.getProductId());

        // when (USER 권한)
        List<ProductResponseDto> userProducts = productService.findProducts(product1.getProductId(), null, null,
                UserRole.USER, null);
        // when (ADMIN 권한)
        List<ProductResponseDto> adminProducts = productService.findProducts(product1.getProductId(), null, null,
                UserRole.ADMIN, null);

        // then
        assertThat(userProducts).isEmpty();
        assertThat(adminProducts).isNotEmpty();
        assertThat(adminProducts.getFirst().getProductStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("멤버 등급에 따라 할인된 가격이 적용된다")
    void findProductsWithDiscountTest() {
        // given
        GradeName bronzeGrade = com.twog.shopping.global.common.entity.GradeName.BRONZE;

        // when
        // BRONZE 등급 (2% 할인): 10000 -> 9800
        List<ProductResponseDto> products = productService.findProducts(null, "Product 1", null, UserRole.USER,
                bronzeGrade);

        // then
        assertThat(products).isNotEmpty();
        ProductResponseDto product = products.getFirst();
        assertThat(product.getDiscountPrice()).isEqualTo(9800);
        assertThat(product.getProductPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("상품 목록을 페이징하여 조회한다")
    void getProductsPageTest() {
        // given
        // setUp에서 2개의 상품 생성됨 (product1: Category A, product2: Category B)
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

        GradeName gradeName = GradeName.BRONZE;
        Pageable pageable = PageRequest.of(0, 3); // 3개씩 조회

        // when
        Page<ProductResponseDto> productsPage = productService.getProductsPage(null, null, "Category P",
                UserRole.USER, gradeName, pageable);

        // then
        assertThat(productsPage.getTotalElements()).isEqualTo(5);
        assertThat(productsPage.getContent()).hasSize(3);
        assertThat(productsPage.getContent().get(0).getProductCategory()).isEqualTo("Category P");
        assertThat(productsPage.getTotalPages()).isEqualTo(2); // 5개니까 2페이지 (3+2)
    }
}
