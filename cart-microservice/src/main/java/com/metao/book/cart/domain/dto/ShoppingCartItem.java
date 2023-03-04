package com.metao.book.cart.domain.dto;

import com.metao.book.shared.domain.financial.Currency;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ShoppingCartItem(
    String asin,
    BigDecimal quantity,
    BigDecimal price,
    Currency currency
) {
}
