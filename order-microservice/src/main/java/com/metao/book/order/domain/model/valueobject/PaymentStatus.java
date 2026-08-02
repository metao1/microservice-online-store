package com.metao.book.order.domain.model.valueobject;

import com.metao.book.order.domain.exception.InvalidPaymentEventException;

public enum PaymentStatus {
    PENDING,
    SUCCESSFUL,
    FAILED,
    CANCELLED;

    public static PaymentStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPaymentEventException("Payment status must not be blank");
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidPaymentEventException("Unsupported payment status: " + value, ex);
        }
    }
}
