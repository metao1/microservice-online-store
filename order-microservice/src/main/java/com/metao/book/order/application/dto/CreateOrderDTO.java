package com.metao.book.order.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.financial.Currency;

import lombok.Builder;

@Builder
public record CreateOrderDTO(
                @JsonProperty("product_id") String productId,
                @JsonProperty("customer_id") String customerId,
                Status status,
                BigDecimal quantity,
                Currency currency,
                BigDecimal price)  implements Serializable {
}
