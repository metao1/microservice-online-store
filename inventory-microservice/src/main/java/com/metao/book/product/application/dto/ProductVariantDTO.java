package com.metao.book.product.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ProductVariantDTO(
    @JsonProperty("id")
    String id,

    @JsonProperty("type")
    String type,

    @JsonProperty("name")
    String name,

    @JsonProperty("value")
    String value,

    @JsonProperty("hexColor")
    String hexColor,

    @JsonProperty("inStock")
    boolean inStock,

    @JsonProperty("priceModifier")
    BigDecimal priceModifier
) {
}
