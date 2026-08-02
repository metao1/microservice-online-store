package com.metao.book.order.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCartItemQtyDto(@NotNull BigDecimal quantity) {
}
