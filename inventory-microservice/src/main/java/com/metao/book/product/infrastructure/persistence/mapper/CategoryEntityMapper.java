package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import com.metao.book.product.infrastructure.persistence.repository.JpaCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper between ProductCategory domain object and CategoryEntity
 */
@Component
@RequiredArgsConstructor
public class CategoryEntityMapper {

    private final JpaCategoryRepository jpaCategoryRepository;

    /**
     * Convert domain ProductCategory to CategoryEntity
     * For existing categories (with ID), fetch from DB to ensure it's attached to session
     */
    public CategoryEntity toEntity(ProductCategory category) {
        // If category has an ID, it exists in DB - fetch it to attach to session
        if (category.getId() != null) {
            return jpaCategoryRepository.findById(category.getId().value())
                .orElseGet(() -> {
                    // Fallback: create new entity if not found by ID
                    CategoryEntity entity = new CategoryEntity(category.getName().value());
                    entity.setId(category.getId().value());
                    return entity;
                });
        }

        // New category without ID - let cascade persist handle it
        return new CategoryEntity(category.getName().value());
    }

    /**
     * Convert CategoryEntity to domain ProductCategory
     */
    public ProductCategory toDomain(CategoryEntity entity) {
        var categoryId = CategoryId.of(entity.getId());
        var categoryName = CategoryName.of(entity.getCategory());

        return ProductCategory.of(categoryId, categoryName);
    }
}
