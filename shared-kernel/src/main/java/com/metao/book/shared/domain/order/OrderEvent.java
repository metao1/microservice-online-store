package com.metao.book.shared.domain.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.Builder;

@Builder
public record OrderEvent
    (
        @JsonProperty("order_id") String orderId,
        @JsonProperty("product_id") String productId,
        @JsonProperty("customer_id") String customerId,
        OrderStatus orderStatus,
        BigDecimal quantity,
        Currency currency,
        BigDecimal price
    ) {
}
