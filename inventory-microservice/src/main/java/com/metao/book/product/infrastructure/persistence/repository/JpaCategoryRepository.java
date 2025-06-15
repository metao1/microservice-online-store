package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for CategoryEntity
 */
@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCategory(String category);

    boolean existsByCategory(String category);
}
