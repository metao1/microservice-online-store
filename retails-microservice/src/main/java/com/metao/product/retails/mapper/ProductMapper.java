package com.metao.product.retails.mapper;

import com.metao.product.retails.config.BaseMappingConfig;
import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.model.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        config = BaseMappingConfig.class,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ProductMapper {

    @Mappings(@Mapping(source = "id", target = "asin"))
    ProductDTO toDto(ProductEntity productEntity);

    @Mappings(@Mapping(source = "asin", target = "id"))
    ProductEntity toEntity(ProductDTO productDTO);

}
