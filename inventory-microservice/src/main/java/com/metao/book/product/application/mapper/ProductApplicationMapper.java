package com.metao.book.product.application.mapper;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper between domain objects and application DTOs
 */
@Component
public class ProductApplicationMapper {

    /**
     * Convert domain Product to ProductDTO
     */
    public ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
            .asin(product.getId().value())
            .title(product.getTitle().value())
            .description(product.getDescription().value())
            .imageUrl(product.getImageUrl().value())
            .price(product.getPrice().fixedPointAmount())
            .currency(product.getPrice().currency())
            .volume(product.getVolume().value())
            .categories(mapCategoriesToNames(product.getCategories()))
            .createdTime(product.getCreatedTime())
            .updatedTime(product.getUpdatedTime())
            .inStock(product.isInStock())
            .build();
    }

    /**
     * Map categories to category names
     */
    private Set<String> mapCategoriesToNames(Set<ProductCategory> categories) {
        return categories.stream()
            .map(category -> category.getName().getValue())
            .collect(Collectors.toSet());
    }
}
