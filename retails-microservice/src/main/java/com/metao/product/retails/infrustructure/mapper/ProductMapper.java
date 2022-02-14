package com.metao.product.retails.infrustructure.mapper;

import com.metao.ddd.finance.Money;
import com.metao.product.retails.application.dto.CategoryDTO;
import com.metao.product.retails.application.dto.ProductDTO;
import com.metao.product.retails.domain.category.CategoryEntity;
import com.metao.product.retails.domain.product.ProductEntity;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ProductMapper {

    default ProductDTO toDto(ProductEntity pr) {
        return ProductDTO.builder()
                .title(pr.getTitle())
                .asin(pr.id().toUUID())
                .avgStars(pr.getAvgStars())
                .numStars(pr.getNumStars())
                .currency(pr.getPriceCurrency())
                .description(pr.getDescription())
                .price(pr.getPriceValue())
                .imageUrl(pr.getImageUrl().getUrl())
                .build();
    }

    default List<ProductDTO> toDtos(List<ProductEntity> allPr) {
        return allPr.stream().map(this::toDto).toList();
    }

    private static List<CategoryEntity> mapStringToCategory(@NonNull List<CategoryDTO> source) {
        return source
                .stream()
                .map(CategoryDTO::getCategory)
                .map(CategoryEntity::new)
                .toList();
    }

    default Optional<ProductEntity> toEntity(@NonNull ProductDTO productDTO) {
        return Optional
                .of(productDTO)
                .map(ProductMapper::buildProductEntity);
    }

    private static ProductEntity buildProductEntity(ProductDTO item) {
        var productEntity = new ProductEntity(item.getTitle(), item.getDescription(), new Money(item.getCurrency(), item.getPrice()));
        var categories = mapStringToCategory(item.getCategories());
        Stream.of(categories)
                .flatMap(Collection::stream)
                .forEach(productEntity::addCategory);
        return productEntity;
    }
}
