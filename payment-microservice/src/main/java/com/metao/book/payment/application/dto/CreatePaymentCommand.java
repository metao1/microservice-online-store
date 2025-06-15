package com.metao.book.payment.application.dto;

import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Command for creating a new payment
 */
public record CreatePaymentCommand(
    @NotNull
    String orderId,

    @NotNull
    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull
    Currency currency,

    @NotNull
    PaymentMethod.Type paymentMethodType,

    String paymentMethodDetails
) {
}
