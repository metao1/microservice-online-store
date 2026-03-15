package com.metao.book.order.application.cart;

import java.math.BigDecimal;
import java.util.Currency;

public record ShoppingCartItem(
    String sku,
    String productTitle,
    BigDecimal quantity,
    BigDecimal price,
    Currency currency
) {
}
