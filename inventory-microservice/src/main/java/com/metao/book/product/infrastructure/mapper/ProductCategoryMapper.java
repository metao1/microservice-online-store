package com.metao.book.product.infrastructure.mapper;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.domain.ProductCategoryEntity;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public class ProductCategoryMapper implements DTOMapper<ProductCategoryEntity, CategoryDTO> {

    @Override
    public CategoryDTO convertToDto(@NonNull ProductCategoryEntity val) {
        return CategoryDTO.of(val.getCategory().category());
    }

    public Set<CategoryDTO> convertToDtoSet(@NonNull Set<ProductCategoryEntity> val) {
        return val.stream().map(this::convertToDto).collect(Collectors.toSet());
    }
}
