package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import io.micrometer.core.annotation.Timed;
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

    @Timed(value = "inventory.db.category.find-by-category")
    @NotNull
    Optional<CategoryEntity> findByCategory(String category);

    @Timed(value = "inventory.db.category.find-ids-by-category-in")
    @Query("""
        select c.id
        from product_category c
        where c.category in :categories
        """)
    List<String> findIdsByCategoryIn(@Param("categories") List<String> categories);

    @Timed(value = "inventory.db.category.exists-by-category")
    boolean existsByCategory(String category);
}
