package com.twog.shopping.domain.product.controller;

import com.twog.shopping.domain.product.dto.ProductRequestDto;
import com.twog.shopping.domain.product.dto.ProductResponseDto;
import com.twog.shopping.domain.product.service.ProductService;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.global.common.entity.GradeName;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "조건에 맞는 상품 목록을 조회합니다. (ID, 이름, 카테고리 등 필터링 가능)")
    public ResponseEntity<List<ProductResponseDto>> getProducts(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCategory,
            @AuthenticationPrincipal DetailsUser user) {

        UserRole userRole = (user != null) ? user.getMember().getMemberRole() : null;
        GradeName gradeName = (user != null) ? user.getGradeName() : null;

        List<ProductResponseDto> products = productService.findProducts(productId, productName, productCategory,
                userRole, gradeName);
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "상품 생성", description = "새로운 상품을 등록합니다. (관리자 전용)")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return ResponseEntity.ok(createdProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다. (관리자 전용)")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable int productId,
            @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto updatedProduct = productService.updateProduct(productId, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다. (관리자 전용)")
    public ResponseEntity<String> deleteProduct(@PathVariable int productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("상품이 성공적으로 삭제되었습니다.");
    }
}