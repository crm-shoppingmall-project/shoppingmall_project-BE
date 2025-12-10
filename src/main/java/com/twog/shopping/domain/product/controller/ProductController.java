package com.twog.shopping.domain.product.controller;

import com.twog.shopping.domain.product.dto.ProductRequestDto;
import com.twog.shopping.domain.product.dto.ProductResponseDto;
import com.twog.shopping.domain.product.service.ProductService;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.global.common.entity.GradeName;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
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
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return ResponseEntity.ok(createdProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable int productId,
            @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto updatedProduct = productService.updateProduct(productId, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable int productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("상품이 성공적으로 삭제되었습니다.");
    }
}