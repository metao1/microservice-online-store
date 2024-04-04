package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.Builder;

@Builder
public record CreateOrderDTO(
    @NotNull @JsonProperty("product_id") String productId,
    @NotNull @JsonProperty("account_id") String accountId,
    @DecimalMin("0") BigDecimal quantity,
    Currency currency,
    @DecimalMin("0") BigDecimal price) implements Serializable {
}
