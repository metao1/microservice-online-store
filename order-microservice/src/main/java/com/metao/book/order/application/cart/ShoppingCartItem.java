package com.metao.book.order.application.cart;

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
