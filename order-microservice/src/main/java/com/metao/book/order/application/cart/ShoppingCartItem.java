package com.metao.book.order.application.cart;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.Currency;

@Builder
public record ShoppingCartItem(
    String sku,
    String productTitle,
    BigDecimal quantity,
    BigDecimal price,
    Currency currency
) {
}
