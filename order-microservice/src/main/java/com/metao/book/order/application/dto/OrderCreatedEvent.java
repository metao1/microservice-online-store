package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.shared.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.Builder;

@Builder
public record OrderCreatedEvent
    (
        @JsonProperty("order_id") String orderId,
        @JsonProperty("product_id") String productId,
        @JsonProperty("customer_id") String customerId,
        OrderStatus status,
        BigDecimal quantity,
        Currency currency,
        BigDecimal price
    ) {
}
