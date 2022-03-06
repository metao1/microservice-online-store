package com.metao.product.domain;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, ProductCategoryId> {

    Optional<ProductCategoryEntity> findProductCategoryByProductId(ProductCategoryId productId);

}

