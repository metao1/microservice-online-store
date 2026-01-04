package com.metao.book.order.presentation.dto;

import java.math.BigDecimal;
import java.util.Currency;
import lombok.Builder;

@Builder
public record ShoppingCartItemDto(
    String sku,
    BigDecimal quantity,
    BigDecimal price,
    Currency currency
) {
}
