package com.metao.book.product.domain.repository;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for ProductCategory entity
 */
public interface CategoryRepository {

    /**
     * Save a category
     */
    ProductCategory save(ProductCategory category);

    /**
     * Find category by ID
     */
    Optional<ProductCategory> findById(CategoryId categoryId);

    /**
     * Find category by name
     */
    Optional<ProductCategory> findByName(CategoryName categoryName);

    /**
     * Check if category exists by name
     */
    boolean existsByName(CategoryName categoryName);

    List<ProductCategory> findAll(int offset, int limit);

    /**
     * Delete category
     */
    void delete(ProductCategory category);
}
