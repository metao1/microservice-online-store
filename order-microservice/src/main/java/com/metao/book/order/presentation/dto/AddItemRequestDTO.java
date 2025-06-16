package com.metao.book.order.presentation.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record AddItemRequestDTO(BigDecimal quantity, BigDecimal price, Currency currency) {
}
