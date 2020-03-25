package com.metao.product.retails.config;

import com.metao.product.models.AutoAwareItemDTO;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.WARN,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG,
        componentModel = "spring"
)
public interface BaseMappingConfig {

    com.metao.product.retails.domain.AutoAwareItemEntity fromDto(AutoAwareItemDTO autoAwareItemDTO);

    AutoAwareItemDTO toDto(com.metao.product.retails.domain.AutoAwareItemEntity autoAwareItem);
}
