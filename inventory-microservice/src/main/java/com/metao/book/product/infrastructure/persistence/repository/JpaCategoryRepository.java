package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import java.util.Optional;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for CategoryEntity
 */
@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, String> {

    @NotNull
    Optional<CategoryEntity> findByCategory(String category);

    @Query("""
        select c.id
        from product_category c
        where c.category in :categories
        """)
    List<String> findIdsByCategoryIn(@Param("categories") List<String> categories);

    boolean existsByCategory(String category);
}
