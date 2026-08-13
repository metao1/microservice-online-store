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
    @JsonProperty("userId")
    String userId,

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
    /**
     * Compatibility constructor for order-originated commands that do not carry a user identity.
     * The order identifier remains the stable fallback identity for this internal flow.
     */
    public CreatePaymentCommand(
        String orderId,
        BigDecimal amount,
        String currency,
        PaymentMethod.Type paymentMethodType,
        String paymentMethodDetails
    ) {
        this(orderId, orderId, amount, currency, paymentMethodType, paymentMethodDetails);
    }
}
