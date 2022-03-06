package com.metao.product.domain;

import com.metao.product.application.exception.ProductNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, ProductId> {

    Optional<ProductEntity> findProductEntityById(ProductId productId) throws ProductNotFoundException;

    Optional<List<ProductEntity>> findAllWithOffsetOptional(int limit, int offset);
}
