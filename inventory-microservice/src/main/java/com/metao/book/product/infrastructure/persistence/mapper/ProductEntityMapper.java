package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
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
    public ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity(
            product.getId(),
            product.getTitle(),
            product.getDescription(),
            product.getVolume(),
            new Money(product.getPrice().currency(),product.getPrice().doubleAmount()),
            product.getImageUrl(),
            product.getCreatedTime(),
            product.getUpdatedTime()
        );

        // Map categories
        product.getCategories().stream()
            .map(categoryEntityMapper::toEntity)
            .forEach(entity::addCategory);

        return entity;
    }

    /**
     * Convert ProductEntity to domain Product
     */
    public Product toDomain(ProductEntity entity) {

        // Map categories
        Set<ProductCategory> categories = entity.getCategories().stream()
            .map(categoryEntityMapper::toDomain)
            .collect(Collectors.toSet());

        return Product.reconstruct(
            entity.getSku(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getVolume(),
            new Money(entity.getPrice().currency(), entity.getPrice().doubleAmount()),
            entity.getImageUrl(),
            entity.getCreatedTime(),
            entity.getUpdateTime(),
            categories
        );
    }
}
