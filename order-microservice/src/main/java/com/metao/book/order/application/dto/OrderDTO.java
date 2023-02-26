package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.financial.Currency;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record OrderDTO
    (
        @JsonProperty("order_id")
        String orderId,
        @JsonProperty("product_id")
        String productId,
        @JsonProperty("customer_id")
        String customerId,
        Status status,
        BigDecimal quantity,
        Currency currency,
        BigDecimal price
    ) {
}
