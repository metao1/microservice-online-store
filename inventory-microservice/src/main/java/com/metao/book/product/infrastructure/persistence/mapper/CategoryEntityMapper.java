package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between ProductCategory domain object and CategoryEntity
 */
@Component
public class CategoryEntityMapper {

    /**
     * Convert domain ProductCategory to CategoryEntity
     */
    public CategoryEntity toEntity(ProductCategory category) {
        CategoryEntity entity = new CategoryEntity(category.getName().getValue());
        if (category.getId() != null) {
            entity.setId(category.getId().value());
        }
        return entity;
    }

    /**
     * Convert CategoryEntity to domain ProductCategory
     */
    public ProductCategory toDomain(CategoryEntity entity) {
        CategoryId categoryId = CategoryId.of(entity.getId());
        CategoryName categoryName = CategoryName.of(entity.getCategory());

        return ProductCategory.reconstruct(categoryId, categoryName);
    }
}
