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
import java.time.Instant;
import java.util.Currency;
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
            .sku(product.getId().getValue())
            .title(product.getTitle().getValue())
            .description(product.getDescription().getValue())
            .imageUrl(product.getImageUrl().getValue())
            .price(product.getMoney().fixedPointAmount())
            .currency(product.getMoney().currency())
            .volume(product.getVolume().getValue())
            .categories(mapCategoriesToNames(product.getCategories()))
            .createdTime(product.getCreatedTime())
            .updatedTime(product.getUpdatedTime())
            .inStock(product.isInStock())
            .build();
    }

    public static Product toDomain(ProductDTO productDTO) {
        var createdTime = Instant.now();
        return new Product(
            ProductSku.of(productDTO.sku()),
            ProductTitle.of(productDTO.title()),
            ProductDescription.of(productDTO.description()),
            ProductVolume.of(productDTO.volume()),
            new Money(productDTO.currency() == null ?
                Currency.getInstance("EUR") : productDTO.currency(), productDTO.price()),
            createdTime,
            createdTime,
            ImageUrl.of(productDTO.imageUrl()),
            productDTO.categories().stream()
                .map(CategoryName::of)
                .map(ProductCategory::of)
                .collect(Collectors.toSet())
        );
    }

    public static Product toDomain(CreateProductDto createProductDto) {
        var createdTime = Instant.now();
        return new Product(
            ProductSku.of(createProductDto.sku()),
            ProductTitle.of(createProductDto.title()),
            ProductDescription.of(createProductDto.description()),
            ProductVolume.of(createProductDto.volume()),
            new Money(createProductDto.currency(), createProductDto.price()),
            createdTime,
            createdTime,
            ImageUrl.of(
                createProductDto.imageUrl() == null
                    ? "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop" :
                    createProductDto.imageUrl()),
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
