package com.metao.book.order.application.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Valid
public record UpdateCartItemQtyDTO(
    @NotNull(message = "quantity cannot be null")
    @DecimalMin(value = "0.00", message = "quantity must be greater than or equal to zero")
    BigDecimal quantity) {
}
