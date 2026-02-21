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
    private static final String DEFAULT_IMAGE_URL = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop";
    private static final String DEFAULT_CURRENCY = "EUR";
    private static final BigDecimal DEFAULT_VOLUME = BigDecimal.ONE;

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

    public static ProductAggregate toDomain(ProductDTO productDTO) {
        var createdTime = Instant.now();
        var title = ProductTitle.of(productDTO.title() != null ? productDTO.title() : DEFAULT_TITLE);
        var description = ProductDescription.of(
            productDTO.description() != null ? productDTO.description() : DEFAULT_DESCRIPTION);
        var volume = Quantity.of(productDTO.volume() != null ? productDTO.volume() : DEFAULT_VOLUME);
        var currency = productDTO.currency() != null ? productDTO.currency() : Currency.getInstance(DEFAULT_CURRENCY);
        var money = new Money(currency, productDTO.price());
        var imageUrl = ImageUrl.of(productDTO.imageUrl() != null ? productDTO.imageUrl() : DEFAULT_IMAGE_URL);

        var categories = mapToProductCategories(productDTO.categories());

        return new ProductAggregate(
            ProductSku.of(productDTO.sku()),
            title,
            description,
            volume,
            money,
            createdTime,
            createdTime,
            imageUrl,
            categories
        );
    }

    public static ProductAggregate toDomain(CreateProductDto createProductDto) {
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
