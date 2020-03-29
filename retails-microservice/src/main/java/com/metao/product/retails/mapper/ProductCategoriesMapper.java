package com.metao.product.retails.mapper;

import com.metao.product.models.ProductCategoriesDTO;
import com.metao.product.retails.config.BaseMappingConfig;
import com.metao.product.retails.domain.ProductCategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = BaseMappingConfig.class)
public interface ProductCategoriesMapper {

    @Mappings(@Mapping(source = "id", target = "id"))
    ProductCategoriesDTO toDto(ProductCategoryEntity productEntity);

    @Mappings(@Mapping(source = "id", target = "id"))
    ProductCategoryEntity toEntity(ProductCategoriesDTO productDTO);

}
