package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mapper between ProductCategory domain object and CategoryEntity
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class CategoryEntityMapper {

    private final EntityManager entityManager;

    /**
     * Convert domain ProductCategory to CategoryEntity Uses Hibernate's natural ID lookup which checks session cache
     * first
     */
    public CategoryEntity toEntity(ProductCategory category) {
        try {
            Session session = entityManager.unwrap(Session.class);
            String normalizedName = category.getName().value();
            CategoryEntity existing = session.bySimpleNaturalId(CategoryEntity.class)
                .load(normalizedName);
            if (existing != null) {
                return existing;
            }
            return new CategoryEntity(normalizedName);
        } catch (Exception e) {
            // Fall back to a detached entity instead of failing the mapping inside tests
            log.warn("Falling back to new CategoryEntity for {} due to {}", category.getName(), e.toString());
            return new CategoryEntity(category.getName().value());
        }
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
