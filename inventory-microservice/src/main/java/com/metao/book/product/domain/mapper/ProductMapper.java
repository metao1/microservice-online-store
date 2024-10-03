package com.metao.book.product.domain.mapper;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.event.Category;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDTO toDto(ProductEntity pr) {
        return ProductDTO.builder().description(pr.getDescription()).title(pr.getTitle()).asin(pr.getAsin())
            .volume(pr.getVolume()).currency(pr.getPriceCurrency()).price(pr.getPriceValue()).boughtTogether(
                pr.getBoughtTogether() != null ? Arrays.stream(pr.getBoughtTogether().split(",")).toList() : null)
            .categories(mapCategoryEntitiesToDTOs(pr.getCategories())).imageUrl(pr.getImageUrl()).build();
    }

    public ProductCreatedEvent toEvent(ProductDTO pr) {
        return ProductCreatedEvent.newBuilder().setAsin(pr.asin()).setTitle(pr.title())
            .setDescription(pr.description() == null ? "" : pr.description())
            .setPrice(pr.price() == null ? 100d : pr.price().doubleValue())
            .setCurrency(pr.currency() == null ? "EUR" : pr.currency().toString())
            .setImageUrl(pr.imageUrl() == null ? "" : pr.imageUrl())
            .setVolume(RandomGenerator.getDefault().nextDouble(0, 1000))
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .addAllBoughtTogether(pr.boughtTogether() != null ? pr.boughtTogether().stream().toList() : List.of())
            .addAllCategories(
                pr.categories().stream().map(dto -> Category.newBuilder().setName(dto.getCategory()).build()).toList())
            .build();
    }

    public ProductEntity toEntity(@NonNull ProductCreatedEvent event) {
        var productEntity = new ProductEntity(event.getAsin(), event.getTitle(), event.getDescription(),
            BigDecimal.valueOf(event.getVolume()),
            new Money(Currency.getInstance(event.getCurrency()), BigDecimal.valueOf(event.getPrice())),
            Optional.of(event.getImageUrl()).orElse(""));
        productEntity.addBoughtTogether(event.getBoughtTogetherList());
        var categories = mapCategoryDTOsToEntities(event.getCategoriesList());
        Stream.of(categories).flatMap(Collection::stream).forEach(productEntity::addCategory);
        return productEntity;
    }

    private static Set<ProductCategoryEntity> mapCategoryDTOsToEntities(@NonNull List<Category> categories) {
        return categories.stream().map(Category::getName).map(ProductCategoryEntity::new).collect(Collectors.toSet());
    }

    private static Set<CategoryDTO> mapCategoryEntitiesToDTOs(@NonNull Set<ProductCategoryEntity> source) {
        return source.stream().map(ProductCategoryEntity::getCategory).map(CategoryDTO::new)
            .collect(Collectors.toSet());
    }

}
