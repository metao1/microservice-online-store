package com.metao.book.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Command for creating a new payment
 */
public record CreatePaymentCommand(
    @NotNull
    @JsonProperty("orderId")
    String orderId,

    @NotNull
    @Positive(message = "Amount must be positive")
    @JsonProperty("amount")
    BigDecimal amount,

    @NotNull
    @JsonProperty("currency")
    String currency,

    @NotNull
    @JsonProperty("paymentMethodType")
    PaymentMethod.Type paymentMethodType,

    @JsonProperty("paymentMethodDetails")
    String paymentMethodDetails
) {
}
