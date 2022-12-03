package com.metao.book.product.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, ProductCategoryId> {

    Optional<List<ProductCategoryEntity>> findAllById(@NonNull ProductCategoryId productCategoryId);
}
