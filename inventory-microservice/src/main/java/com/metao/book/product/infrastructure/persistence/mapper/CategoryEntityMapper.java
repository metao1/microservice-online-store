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
        Session session = entityManager.unwrap(Session.class);
        // First, try to find by natural ID (category name) in session cache + DB
        CategoryEntity existing;
        try {
            existing = session.bySimpleNaturalId(CategoryEntity.class).load(category.getName().value());
        } catch (Exception e) {
            log.error("Failed to load CategoryEntity with name {}", category.getName(), e);
            throw new RuntimeException(e);
        }
        // Found in session or DB - return the managed entity
        return Objects.requireNonNullElseGet(existing, () -> new CategoryEntity(category.getName().value()));
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
