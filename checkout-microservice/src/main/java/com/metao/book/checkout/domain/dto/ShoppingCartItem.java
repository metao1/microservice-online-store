package com.metao.book.checkout.domain.dto;

import java.math.BigDecimal;
import java.util.Currency;
import lombok.Builder;

@Builder
public record ShoppingCartItem(
    String asin,
    BigDecimal quantity,
    BigDecimal price,
    Currency currency
) {
}
