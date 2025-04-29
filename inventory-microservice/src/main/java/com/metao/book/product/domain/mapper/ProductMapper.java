package com.metao.book.product.domain.mapper;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.CategoryOuterClass.Category;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.book.shared.domain.financial.Money;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductMapper {

    public static ProductDTO toDto(@NotNull Product pr) throws NullPointerException {
        return ProductDTO.builder()
            .description(pr.getDescription())
            .title(pr.getTitle())
            .asin(pr.getAsin())
            .volume(pr.getVolume())
            .currency(pr.getPriceCurrency())
            .price(pr.getPriceValue())
            .categories(mapCategoryEntitiesToDTOs(pr.getCategories()))
            .imageUrl(pr.getImageUrl())
            .build();
    }

    public static ProductCreatedEvent toProductCreatedEvent(ProductDTO pr) throws NullPointerException {
        return ProductCreatedEvent.newBuilder()
            .setAsin(pr.asin())
            .setTitle(pr.title() != null ? pr.title() : "No title")
            .setDescription(pr.description() != null ? pr.description() : "")
            .setPrice(pr.price() != null ? pr.price().doubleValue() : BigDecimal.ZERO.doubleValue())
            .setCurrency(pr.currency() != null ? pr.currency().toString() : "EUR")
            .setImageUrl(pr.imageUrl() != null ? pr.imageUrl() : "")
            .setVolume(pr.volume() != null ? pr.volume().doubleValue() : BigDecimal.ZERO.doubleValue())
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .addAllBoughtTogether(pr.boughtTogether() != null ? pr.boughtTogether() : List.of())
            .addAllCategories(mapToCategories(pr))
            .build();
    }

    public static Product fromDto(@NonNull ProductDTO pr) {
        var pe = new Product(pr.asin(),
            pr.title(),
            pr.description(),
            pr.volume(),
            new Money(pr.currency(), pr.price()),
            Optional.of(pr.imageUrl()).orElse(""));
        pe.addCategories(mapCategoryDTOsToEntities(mapToCategories(pr)));
        return pe;
    }

    public static Product fromProductCreatedEvent(@NonNull ProductCreatedEvent event) {
        var pe = new Product(event.getAsin(),
            event.getTitle(),
            event.getDescription(),
            BigDecimal.valueOf(event.getVolume()),
            new Money(Currency.getInstance(event.getCurrency()), BigDecimal.valueOf(event.getPrice())),
            Optional.of(event.getImageUrl()).orElse(""));
        pe.addCategories(mapCategoryDTOsToEntities(event.getCategoriesList()));
        return pe;
    }

    public static Product fromProductUpdatedEvent(@NonNull ProductUpdatedEvent event) {
        var pe = new Product(event.getAsin(),
            event.getTitle(),
            event.getDescription(),
            BigDecimal.valueOf(event.getVolume()),
            new Money(Currency.getInstance(event.getCurrency()), BigDecimal.valueOf(event.getPrice())),
            Optional.of(event.getImageUrl()).orElse(""));

        pe.setUpdateTime(LocalDateTime.from(Instant.ofEpochSecond(event.getUpdatedTime().getSeconds())));
        pe.addCategories(mapCategoryDTOsToEntities(event.getCategoriesList()));
        return pe;
    }

    private static List<Category> mapToCategories(ProductDTO pr) {
        return pr.categories() != null ?
            pr.categories().stream().map(dto -> Category.newBuilder().setName(dto.category()).build())
                .toList() : List.of();
    }

    private static Set<ProductCategory> mapCategoryDTOsToEntities(@NonNull List<Category> categories) {
        return categories.stream()
            .map(Category::getName)
            .map(ProductCategory::new)
            .collect(Collectors.toSet());
    }

    private static Set<CategoryDTO> mapCategoryEntitiesToDTOs(@NonNull Set<ProductCategory> source) {
        return source.stream()
            .map(ProductCategory::getCategory)
            .filter(Objects::nonNull)
            .map(CategoryDTO::new)
            .collect(Collectors.toSet());
    }

}
