package com.metao.product.retails.mapper;

import com.metao.product.models.ProductDTO;
import com.metao.product.retails.config.BaseMappingConfig;
import com.metao.product.retails.domain.ProductCategoryEntity;
import com.metao.product.retails.domain.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = BaseMappingConfig.class)
public interface ProductMapper {

    @Mappings({@Mapping(source = "id", target ="asin"), @Mapping(target = "categories", ignore = true)})
    ProductDTO toDto(ProductEntity productEntity);

    default ProductDTO mapToDto(ProductEntity source) {
        ProductDTO productDTO = toDto(source);
        productDTO.setCategories(mapCategoriesToString(source.getCategories()));
        return productDTO;
    }

    default Set<String> mapCategoriesToString(Set<ProductCategoryEntity> source) {
        return source.stream().map(ProductCategoryEntity::getCategories).collect(Collectors.toSet());
    }

    @Mappings({@Mapping(source = "asin", target = "id"), @Mapping(target = "categories", ignore = true)})
    ProductEntity toEntity(ProductDTO productDTO);

    default Set<ProductCategoryEntity> mapStringToCategories(Set<String> source) {
        return source.stream().map(category -> ProductCategoryEntity.builder()
                .categories(category).id(UUID.randomUUID().toString()).build()).collect(Collectors.toSet());
    }

    default ProductEntity mapToEntity(ProductDTO productDTO) {
        Set<ProductCategoryEntity> productCategoryEntities = mapStringToCategories(productDTO.getCategories());
        ProductEntity productEntity = toEntity(productDTO);
        productEntity.setCategories(productCategoryEntities);
        return productEntity;
    }

}
