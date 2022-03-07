package com.metao.product.infrustructure.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import com.metao.ddd.shared.domain.financial.Money;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.category.Category;
import com.metao.product.domain.image.Image;

import org.springframework.lang.NonNull;

public interface ProductMapperInterface {

    default ProductDTO toDto(@Valid ProductEntity pr) {
        return ProductDTO.builder()
                .title(pr.getTitle())
                .asin(pr.id().toUUID())
                .currency(pr.getPriceCurrency())
                .description(pr.getDescription())
                .price(pr.getPriceValue())
                .categories(mapCategoryEntitieToDTOs(pr.getProductCategory()))
                .imageUrl(pr.getImage().url())
                .build();
    }

    default Optional<ProductEntity> toEntity(@NonNull ProductDTO productDTO) {
        return Optional
                .of(productDTO)
                .map(ProductMapperInterface::buildProductEntity);
    }

    default List<ProductDTO> toDtos(@NonNull List<ProductEntity> allPr) {
        return allPr.stream().map(this::toDto).toList();
    }

    private static Set<CategoryDTO> mapCategoryEntitieToDTOs(@NonNull Set<ProductCategoryEntity> source) {
        return source
                .stream()
                .map(ProductCategoryEntity::getCategory)
                .map(Category::category)
                .map(CategoryDTO::of)
                .collect(Collectors.toSet());
    }

    private static Set<ProductCategoryEntity> mapCategoryDTOsToEntities(@NonNull Set<CategoryDTO> source) {
        return source
                .stream()
                .map(CategoryDTO::getCategory)
                .map(Category::new)
                .map(ProductCategoryEntity::new)
                .distinct()
                .collect(Collectors.toSet());
    }

    private static ProductEntity buildProductEntity(ProductDTO item) {
        var productEntity = new ProductEntity(
                item.getTitle(),
                item.getDescription(),
                new Money(item.getCurrency(), item.getPrice()),
                new Image(item.getImageUrl()));
        var categories = mapCategoryDTOsToEntities(item.getCategories());
        Stream.of(categories)
                .flatMap(Collection::stream)
                .forEach(productEntity::addCategory);
        return productEntity;
    }
}
