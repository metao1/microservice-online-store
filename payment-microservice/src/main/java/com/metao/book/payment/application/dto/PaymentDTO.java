package com.metao.book.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import lombok.Builder;

/**
 * Payment Data Transfer Object for application layer
 */
@Builder
public record PaymentDTO(
    @JsonProperty("paymentId")
    String paymentId,

    @JsonProperty("orderId")
    String orderId,

    @JsonProperty("amount")
    BigDecimal amount,

    @JsonProperty("currency")
    Currency currency,

    @JsonProperty("paymentMethod")
    String paymentMethodType,

    @JsonProperty("paymentMethodDetails")
    String paymentMethodDetails,

    @JsonProperty("status")
    String status,

    @JsonProperty("failureReason")
    String failureReason,

    @JsonProperty("processedAt")
    LocalDateTime processedAt,

    @JsonProperty("createdAt")
    LocalDateTime createdAt,

    @JsonProperty("isCompleted")
    boolean isCompleted,

    @JsonProperty("isSuccessful")
    boolean isSuccessful
) {
}
