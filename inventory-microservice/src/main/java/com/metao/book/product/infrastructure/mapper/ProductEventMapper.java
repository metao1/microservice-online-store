package com.metao.book.product.infrastructure.mapper;

import com.google.protobuf.Timestamp;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.image.Image;
import com.metao.book.product.event.Category;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProductEventMapper {

    public ProductCreatedEvent toEvent(ProductDTO pr) {
        try {
            return ProductCreatedEvent.newBuilder()
                .setAsin(pr.asin())
                .setTitle(pr.title())
                .setDescription(pr.description() == null ? "" : pr.description())
                .setPrice(pr.price() == null ? 0 : pr.price().doubleValue())
                .setCurrency(pr.currency() == null ? Currency.getInstance("EUR").toString()
                    : pr.currency().toString())
                .setImageUrl(pr.imageUrl() == null ? "" : pr.imageUrl())
                .setVolume(pr.volume() == null ? 0 : pr.volume().doubleValue())
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .addAllCategories(
                    pr.categories().stream().map(dto -> Category.newBuilder().setName(dto.getCategory()).build())
                        .toList()
                ).build();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public ProductEntity toEntity(@NonNull ProductCreatedEvent event) {
        var productEntity = new ProductEntity(
            event.getAsin(),
            event.getTitle(),
            event.getDescription(),
            BigDecimal.valueOf(event.getVolume()),
            new Money(Currency.getInstance(event.getCurrency()), BigDecimal.valueOf(event.getPrice())),
            new Image(Optional.of(event.getImageUrl()).orElse("")));
        var categories = mapCategoryDTOsToEntities(event.getCategoriesList());
        Stream.of(categories)
            .flatMap(Collection::stream)
            .forEach(productEntity::addCategory);
        return productEntity;
    }

    private static Set<ProductCategoryEntity> mapCategoryDTOsToEntities(@NonNull List<Category> categories) {
        return categories
            .stream()
            .map(Category::getName)
            .map(com.metao.book.product.domain.category.Category::new)
            .map(ProductCategoryEntity::new)
            .collect(Collectors.toSet());
    }


}
