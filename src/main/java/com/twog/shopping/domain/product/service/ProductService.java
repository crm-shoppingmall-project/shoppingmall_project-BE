package com.twog.shopping.domain.product.service;

import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.global.error.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.twog.shopping.domain.product.dto.ProductRequestDto;
import com.twog.shopping.domain.product.dto.ProductResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository; // final 추가

    // 신상품 추가
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = Product.builder()
                .productName(requestDto.getProductName())
                .productCategory(requestDto.getProductCategory())
                .productPrice(requestDto.getProductPrice())
                .productQuantity(requestDto.getProductQuantity())
                .productStatus(requestDto.getProductStatus())
                .build();
        Product savedProduct = productRepository.save(product);
        return new ProductResponseDto(savedProduct);
    }

    // 상품 정보 수정
    @Transactional
    public ProductResponseDto updateProduct(int productId, ProductRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 상품을 찾을 수 없습니다."));

        product.updateProductInfo(
                requestDto.getProductName(),
                requestDto.getProductCategory(),
                requestDto.getProductQuantity(),
                requestDto.getProductPrice(),
                requestDto.getProductStatus());

        return new ProductResponseDto(product);
    }

    // 상품 삭제 (논리적 삭제: DELETED 상태로 변경)
    @Transactional
    public void deleteProduct(int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("해당 상품을 찾을 수 없습니다."));

        // 논리적 삭제 수행
        product.delete();
    }

    // 상품 조회 (검색 및 필터링)
    // ADMIN이 아닌 경우 DELETED 상태의 상품은 조회되지 않음
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProducts(Integer productId, String productName, String productCategory,
            UserRole userRole) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productId != null) {
                predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
            }
            if (productName != null && !productName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")),
                        "%" + productName.toLowerCase() + "%"));
            }
            if (productCategory != null && !productCategory.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("productCategory"), productCategory));
            }

            // ADMIN이 아니면 삭제된 상품은 조회되지 않도록 필터링
            if (userRole != UserRole.ADMIN) {
                predicates.add(criteriaBuilder.notEqual(root.get("productStatus"),
                        com.twog.shopping.domain.product.entity.ProductStatus.DELETED));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec).stream()
                .map(ProductResponseDto::new)
                .toList();
    }
}