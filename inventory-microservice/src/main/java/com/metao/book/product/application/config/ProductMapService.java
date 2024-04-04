package com.metao.book.product.application.config;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.domain.image.Image;
import com.metao.book.shared.domain.financial.Money;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProductMapService {

    public ProductEntity toEntity(@NonNull ProductEvent event) {
        var productEntity = new ProductEntity(
            event.asin(),
            event.title(),
            event.description(),
            event.volume(),
            new Money(event.currency(), event.price()),
            new Image(Optional.ofNullable(event.imageUrl()).orElse("")));
        var categories = mapCategoryDTOsToEntities(event.categories());
        Stream.of(categories)
            .flatMap(Collection::stream)
            .forEach(productEntity::addCategory);
        return productEntity;
    }

    public List<ProductEvent> toDTOList(@NonNull List<ProductEntity> allPr) {
        return allPr.stream().map(this::toEvent).toList();
    }

    public ProductEvent toEvent(@Valid ProductEntity pr) {
        return ProductEvent.builder()
            .description(pr.getDescription())
            .title(pr.getTitle())
            .asin(pr.getAsin())
            .volume(pr.getVolume())
            .currency(pr.getPriceCurrency())
            .price(pr.getPriceValue())
            .categories(mapCategoryEntitiesToDTOs(pr.getCategories()))
            .imageUrl(pr.getImage().url())
            .build();
    }

    private static Set<ProductCategoryEntity> mapCategoryDTOsToEntities(@NonNull Set<CategoryDTO> source) {
        return source
            .stream()
            .map(CategoryDTO::getCategory)
            .map(Category::new)
            .map(ProductCategoryEntity::new)
            .collect(Collectors.toSet());
    }

    private static Set<CategoryDTO> mapCategoryEntitiesToDTOs(@NonNull Set<ProductCategoryEntity> source) {
        return source
            .stream()
            .map(ProductCategoryEntity::getCategory)
            .map(Category::category)
            .map(CategoryDTO::of)
            .collect(Collectors.toSet());
    }

}
