package com.twog.shopping.domain.product.repository;

import com.twog.shopping.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    Optional<Product> findProductByProductId(int productId);

    Optional<Product> findProductByProductName(String productName);

    Optional<Product> findProductByProductCategory(String productCategory);

    Optional<Product> findProductByProductPrice(int productPrice);
}
