package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.shared.domain.financial.Money;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper between Product domain object and ProductEntity
 */
@Component
@RequiredArgsConstructor
public class ProductEntityMapper {

    private final CategoryEntityMapper categoryEntityMapper;

    /**
     * Convert domain Product to ProductEntity
     */
    public ProductEntity toEntity(ProductAggregate product) {
        ProductEntity entity = new ProductEntity(
            product.getId(),
            product.getTitle(),
            product.getDescription(),
            product.getVolume(),
            Money.of(product.getMoney().currency(), product.getMoney().doubleAmount()),
            product.getImageUrl(),
            product.getCreatedTime(),
            product.getUpdatedTime()
        );
        entity.setVersion(product.getVersion());

        // Map categories
        product.getCategories().stream()
            .map(categoryEntityMapper::toEntity)
            .forEach(entity::addCategory);

        return entity;
    }

    /**
     * Convert ProductEntity to domain Product
     */
    public ProductAggregate toDomain(ProductEntity entity) {
        // Map categories
        Set<ProductCategory> categories = entity.getCategories().stream()
            .map(categoryEntityMapper::toDomain)
            .collect(Collectors.toSet());

        return toDomain(entity, categories);
    }

    public ProductAggregate toDomain(ProductEntity entity, Set<ProductCategory> categories) {
        return new ProductAggregate(
            entity.getSku(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getVolume(),
            Money.of(entity.getPrice().currency(), entity.getPrice().doubleAmount()),
            entity.getCreatedTime(),
            entity.getUpdateTime(),
            entity.getImageUrl(),
            categories,
            entity.getVersion()
        );
    }

    public ProductCategory toDomain(String categoryId, String categoryName) {
        return ProductCategory.of(CategoryId.of(categoryId), CategoryName.of(categoryName));
    }
}
