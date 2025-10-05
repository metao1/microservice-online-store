package com.metao.book.product.application.mapper;

import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.shared.domain.financial.Money;
import java.time.LocalDateTime;
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
            .sku(product.getId().value())
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

    public static Product toDomain(CreateProductDto createProductDto) {
        return Product.reconstruct(
            ProductSku.of(createProductDto.sku()),
            ProductTitle.of(createProductDto.title()),
            ProductDescription.of(createProductDto.description()),
            ProductVolume.of(createProductDto.volume()),
            new Money(createProductDto.currency(), createProductDto.price()),
            ImageUrl.of(createProductDto.imageUrl()),
            LocalDateTime.now(),
            LocalDateTime.now(),
            createProductDto.categories().stream()
                .map(CategoryName::of)
                .map(ProductCategory::of)
                .collect(Collectors.toSet())
        );
    }

    /**
     * Map categories to category names
     */
    private Set<String> mapCategoriesToNames(Set<ProductCategory> categories) {
        return categories.stream()
            .map(category -> category.getName().value())
            .collect(Collectors.toSet());
    }
}
