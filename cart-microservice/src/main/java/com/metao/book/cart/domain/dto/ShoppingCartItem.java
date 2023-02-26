package com.metao.book.cart.domain.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ShoppingCartItem(
    String userId,
    String asin,
    BigDecimal quantity,
    BigDecimal price
    ) {
}
