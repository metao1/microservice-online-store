package com.metao.book.product.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.metao.book.product.application.config.CurrencyDeserializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Set;
import lombok.Builder;

/**
 * Product Data Transfer Object for application layer
 */
@Builder
public record ProductDTO(
    @JsonProperty("sku")
    String sku,

    @JsonProperty("title")
    String title,

    @JsonProperty("description")
    String description,

    @JsonProperty("imageUrl")
    String imageUrl,

    @JsonProperty("price")
    BigDecimal price,

    @JsonDeserialize(using = CurrencyDeserializer.class)
    @JsonProperty("currency")
    Currency currency,

    @JsonProperty("volume")
    BigDecimal volume,

    @JsonProperty("categories")
    Set<String> categories,

    @JsonProperty("createdTime")
    LocalDateTime createdTime,

    @JsonProperty("updatedTime")
    LocalDateTime updatedTime,

    @JsonProperty("inStock")
    boolean inStock
) {
}
