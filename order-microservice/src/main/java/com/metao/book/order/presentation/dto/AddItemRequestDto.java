package com.metao.book.order.presentation.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record AddItemRequestDto(
    String sku,
    int quantity,
    BigDecimal unitPrice,
    Currency currency
) {

}