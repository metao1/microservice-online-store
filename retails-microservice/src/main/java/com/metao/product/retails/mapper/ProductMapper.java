package com.metao.product.retails.mapper;

import com.metao.product.retails.config.BaseMappingConfig;
import com.metao.product.retails.domain.ProductCategoryEntity;
import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.model.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = BaseMappingConfig.class)
public interface ProductMapper extends BaseMappingConfig {

    @Mappings({@Mapping(source = "id", target = "asin"), @Mapping(source = "categories.categories", target = "categories", ignore = true)})
    ProductDTO toDto(ProductEntity productEntity);

    default ProductDTO mapToDto(ProductEntity source) {
        validateEntity(source);
        ProductDTO productDTO = toDto(source);
        productDTO.setCategories(mapCategoriesToString(source.getCategories()));
        return productDTO;
    }

    default Set<String> mapCategoriesToString(@NotBlank Set<ProductCategoryEntity> source) {
        return source.stream()
                .map(ProductCategoryEntity::getCategories)
                .collect(Collectors.toSet());
    }

    @Mappings({@Mapping(source = "asin", target = "id"), @Mapping(target = "categories", ignore = true)})
    ProductEntity toEntity(ProductDTO productDTO);

    default Set<ProductCategoryEntity> mapStringToCategories(Set<String> source) {
        return source.stream().map(category -> ProductCategoryEntity.builder()
                .id(UUID.randomUUID().toString())
                .categories(category)
                .build())
                .collect(Collectors.toSet());
    }

    default ProductEntity mapToEntity(ProductDTO productDTO) {
        Set<ProductCategoryEntity> productCategoryEntities = mapStringToCategories(productDTO.getCategories());
        ProductEntity productEntity = toEntity(productDTO);
        productEntity.setCategories(productCategoryEntities);
        return productEntity;
    }

    default void validateEntity(@NotNull ProductEntity source) {
        Objects.requireNonNull(source.getId());
        Objects.requireNonNull(source.getCategories());
        Objects.requireNonNull(source.getPrice());
        Objects.requireNonNull(source.getTitle());
    }
}
