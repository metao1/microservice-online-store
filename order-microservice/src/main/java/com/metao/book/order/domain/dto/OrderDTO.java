package com.metao.book.order.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record OrderDTO(
    @JsonProperty("order_id") String orderId,
    @NotNull @JsonProperty("product_id") String productId,
    @NotNull @JsonProperty("customer_id") String customerId,
    @Positive BigDecimal quantity,
    String currency,
    String status,
    @Positive BigDecimal price) implements Serializable {
}
