package com.metao.book.product.application.mapper;

import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mapper between domain objects and application DTOs
 */
@Component
public class ProductApplicationMapper {

    private static final String DEFAULT_TITLE = "Untitled";
    private static final String DEFAULT_DESCRIPTION = "No description provided";
    private static final String DEFAULT_IMAGE_URL = "https://ecx.images-amazon.com/images/I/51QBqN3F8hL._SY300_.jpg";
    private static final String DEFAULT_CURRENCY = "EUR";

    /**
     * Convert domain Product to ProductDTO
     */
    public ProductDTO toDTO(ProductAggregate product) {
        return ProductDTO.builder()
            .sku(product.getId().value())
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

    public static ProductDTO validateAndSetDefault(ProductDTO productDTO) {
        return ProductDTO.builder()
            .sku(productDTO.sku())
            .title(productDTO.title() != null ? productDTO.title() : DEFAULT_TITLE)
            .description(productDTO.description() != null ? productDTO.description() : DEFAULT_DESCRIPTION)
            .currency(productDTO.currency() != null ? productDTO.currency() : Currency.getInstance(DEFAULT_CURRENCY))
            .createdTime(productDTO.createdTime())
            .volume(productDTO.volume() == null ? BigDecimal.TEN : productDTO.volume())
            .price(productDTO.price() == null ? BigDecimal.TEN : productDTO.price())
            .imageUrl(productDTO.imageUrl() != null ? productDTO.imageUrl() : DEFAULT_IMAGE_URL)
            .categories(productDTO.categories())
            .build();
    }

    public static ProductAggregate validateAndSetDefault(CreateProductDto createProductDto) {
        var createdTime = Instant.now();

        var imageUrl = ImageUrl.of(
            createProductDto.imageUrl() != null
                ? createProductDto.imageUrl()
                : DEFAULT_IMAGE_URL);

        var categories = mapToProductCategories(createProductDto.categories());

        return new ProductAggregate(
            ProductSku.of(createProductDto.sku()),
            ProductTitle.of(createProductDto.title()),
            ProductDescription.of(createProductDto.description()),
            Quantity.of(createProductDto.volume()),
            new Money(createProductDto.currency(), createProductDto.price()),
            createdTime,
            createdTime,
            imageUrl,
            categories
        );
    }

    /**
     * Map categories to category names
     */
    private Set<String> mapCategoriesToNames(Set<ProductCategory> categories) {
        return categories.stream()
            .map(category -> StringUtils.capitalize(category.getName().value()))
            .collect(Collectors.toSet());
    }

    /**
     * Convert category strings to ProductCategory objects with proper type handling
     */
    private static Set<ProductCategory> mapToProductCategories(Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return new HashSet<>();
        }
        return categories.stream()
            .map(CategoryName::of)
            .map(ProductCategory::of)
            .collect(Collectors.toSet());
    }
}
