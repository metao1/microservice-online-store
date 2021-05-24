package com.metao.product.retails.mapper;


import com.metao.product.retails.config.BaseMappingConfig;
import com.metao.product.retails.domain.ProductCategoryEntity;
import com.metao.product.retails.model.ProductCategoriesDTO;
import org.mapstruct.Mapper;

@Mapper(config = BaseMappingConfig.class)
public interface ProductCategoriesMapper {

    ProductCategoriesDTO toDto(ProductCategoryEntity productEntity);
}
