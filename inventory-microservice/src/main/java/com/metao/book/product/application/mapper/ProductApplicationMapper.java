package com.metao.book.product.application.mapper;

import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.dto.ProductVariantDTO;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private static final Set<String> COLOR_CATEGORY_KEYWORDS = Set.of(
        "clothing",
        "apparel",
        "fashion",
        "shoe",
        "shoes",
        "footwear",
        "sneaker",
        "boot",
        "boots",
        "accessories",
        "jewelry",
        "bag",
        "bags",
        "handbag",
        "wallet"
    );
    private static final Set<String> SIZE_CATEGORY_KEYWORDS = Set.of(
        "clothing",
        "apparel",
        "fashion",
        "shoe",
        "shoes",
        "footwear",
        "sneaker",
        "boot",
        "boots"
    );
    private static final List<ColorOption> COLOR_OPTIONS = List.of(
        new ColorOption("Black", "#000000"),
        new ColorOption("Navy", "#1e3a8a"),
        new ColorOption("Gray", "#6b7280"),
        new ColorOption("White", "#ffffff"),
        new ColorOption("Red", "#dc2626"),
        new ColorOption("Blue", "#2563eb"),
        new ColorOption("Green", "#16a34a"),
        new ColorOption("Brown", "#8b4513")
    );
    private static final List<String> SIZE_OPTIONS = List.of("XS", "S", "M", "L", "XL", "XXL");

    /**
     * Convert domain Product to ProductDTO
     */
    public ProductDTO toDTO(ProductAggregate product) {
        var categoryNames = mapCategoriesToNames(product.getCategories());
        return ProductDTO.builder()
            .sku(product.getId().value())
            .title(product.getTitle().value())
            .description(product.getDescription().value())
            .imageUrl(product.getImageUrl().getValue())
            .price(product.getMoney().fixedPointAmount())
            .currency(product.getMoney().currency())
            .volume(product.getVolume().value())
            .categories(categoryNames)
            .variants(buildVariants(product, categoryNames))
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
            .variants(productDTO.variants())
            .inStock(productDTO.inStock())
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
            Money.of(createProductDto.currency(), createProductDto.price()),
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

    private List<ProductVariantDTO> buildVariants(ProductAggregate product, Set<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return List.of();
        }
        var normalizedCategories = categoryNames.stream()
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        boolean hasColor = matchesAny(normalizedCategories, COLOR_CATEGORY_KEYWORDS);
        boolean hasSize = matchesAny(normalizedCategories, SIZE_CATEGORY_KEYWORDS);

        if (!hasColor && !hasSize) {
            return List.of();
        }

        List<ProductVariantDTO> variants = new ArrayList<>();
        var sku = product.getId().value();
        if (hasColor) {
            variants.addAll(buildColorVariants(sku));
        }
        if (hasSize) {
            variants.addAll(buildSizeVariants(sku));
        }
        return variants;
    }

    private boolean matchesAny(Set<String> categories, Set<String> keywords) {
        return categories.stream()
            .anyMatch(category -> keywords.stream().anyMatch(category::contains));
    }

    private List<ProductVariantDTO> buildColorVariants(String sku) {
        List<ProductVariantDTO> variants = new ArrayList<>();
        for (int i = 0; i < COLOR_OPTIONS.size(); i++) {
            var color = COLOR_OPTIONS.get(i);
            variants.add(new ProductVariantDTO(
                "color-" + sku + "-" + i,
                "color",
                color.name(),
                color.hex(),
                color.hex(),
                true,
                BigDecimal.ZERO
            ));
        }
        return variants;
    }

    private List<ProductVariantDTO> buildSizeVariants(String sku) {
        List<ProductVariantDTO> variants = new ArrayList<>();
        for (int i = 0; i < SIZE_OPTIONS.size(); i++) {
            var size = SIZE_OPTIONS.get(i);
            variants.add(new ProductVariantDTO(
                "size-" + sku + "-" + i,
                "size",
                size,
                size,
                null,
                true,
                BigDecimal.ZERO
            ));
        }
        return variants;
    }

    private record ColorOption(String name, String hex) {
    }
}
