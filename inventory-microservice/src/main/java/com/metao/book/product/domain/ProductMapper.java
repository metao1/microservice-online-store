package com.metao.book.product.domain;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductDTO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public List<ProductDTO> toDTOList(@NonNull List<ProductEntity> allPr) {
        return allPr.stream().map(this::toDto).toList();
    }

    public ProductDTO toDto(ProductEntity pr) {
        return ProductDTO.builder()
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

    private static Set<CategoryDTO> mapCategoryEntitiesToDTOs(@NonNull Set<ProductCategoryEntity> source) {
        return source
            .stream()
            .map(ProductCategoryEntity::getCategory)
            .map(com.metao.book.product.domain.category.Category::getValue)
            .map(CategoryDTO::new)
            .collect(Collectors.toSet());
    }

}
