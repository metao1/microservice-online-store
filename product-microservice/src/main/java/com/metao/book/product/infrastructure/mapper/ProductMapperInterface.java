package com.metao.book.product.infrastructure.mapper;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.domain.image.Image;
import com.metao.book.shared.domain.financial.Money;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ProductMapperInterface {

    private static Set<ProductCategoryEntity> mapCategoryDTOsToEntities(@NonNull Set<CategoryDTO> source) {
        return source
                .stream()
                .map(CategoryDTO::getCategory)
                .map(Category::new)
                .map(ProductCategoryEntity::new)
                .collect(Collectors.toSet());
    }

    private static ProductEntity buildProductEntity(ProductDTO item) {
        var productEntity = new ProductEntity(
                item.getAsin(),
                item.getTitle(),
                item.getDescription(),
                new Money(item.getCurrency(), item.getPrice()),
                new Image(Optional.ofNullable(item.getImageUrl()).orElse("")));
        var categories = mapCategoryDTOsToEntities(item.getCategories());
        Stream.of(categories)
                .flatMap(Collection::stream)
                .forEach(productEntity::addCategory);
        return productEntity;
    }

    default Optional<ProductEntity> toEntity(@NonNull ProductDTO productDTO) {
        return Optional
                .of(productDTO)
                .map(ProductMapperInterface::buildProductEntity);
    }

    default List<ProductDTO> toDtos(@NonNull List<ProductEntity> allPr) {
        return allPr.stream().map(ProductEntity::toDto).toList();
    }
}
