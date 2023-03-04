package com.metao.book.cart.domain.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ShoppingCartItem(
    String asin,
    BigDecimal quantity,
    BigDecimal price
    ) {
}
