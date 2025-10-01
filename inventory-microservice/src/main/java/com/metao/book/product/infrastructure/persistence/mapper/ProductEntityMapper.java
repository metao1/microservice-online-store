package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
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
            product.getId().value(),
            product.getTitle().value(),
            product.getDescription().value(),
            product.getVolume().value(),
            new Money(product.getPrice().currency(),product.getPrice().doubleAmount()),
            product.getImageUrl().value()
        );

        entity.setCreatedTime(product.getCreatedTime());
        entity.setUpdateTime(product.getUpdatedTime());

        // Map categories
        Set<CategoryEntity> categoryEntities = product.getCategories().stream()
            .map(categoryEntityMapper::toEntity)
            .collect(Collectors.toSet());
        entity.setCategories(categoryEntities);

        return entity;
    }

    /**
     * Convert ProductEntity to domain Product
     */
    public Product toDomain(ProductEntity entity) {
        ProductSku productSku = ProductSku.of(entity.getSku());
        ProductTitle title = ProductTitle.of(entity.getTitle());
        ProductDescription description = ProductDescription.of(entity.getDescription());
        ProductVolume volume = ProductVolume.of(entity.getVolume());
        Money price = new Money(entity.getPriceCurrency(), entity.getPriceValue());
        ImageUrl imageUrl = ImageUrl.of(entity.getImageUrl());

        // Map categories
        Set<ProductCategory> categories = entity.getCategories().stream()
            .map(categoryEntityMapper::toDomain)
            .collect(Collectors.toSet());

        return Product.reconstruct(
            productSku,
            title,
            description,
            volume,
            price,
            imageUrl,
            entity.getCreatedTime(),
            entity.getUpdateTime(),
            categories
        );
    }
}
