package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record OrderDTO(
    @NotNull @JsonProperty("product_id") String productId,
    @NotNull @JsonProperty("customer_id") String customerId,
    @JsonProperty("created_time") OffsetDateTime createdTime,
    @DecimalMin("0") BigDecimal quantity,
    String currency,
    String status,
    @DecimalMin("0") BigDecimal price) implements Serializable {
}
