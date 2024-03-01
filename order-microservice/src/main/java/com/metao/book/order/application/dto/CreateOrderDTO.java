package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.shared.domain.financial.Currency;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CreateOrderDTO(
                @JsonProperty("product_id") String productId,
                @JsonProperty("account_id") String accountId,
                BigDecimal quantity,
                Currency currency,
                BigDecimal price)  implements Serializable {
}
