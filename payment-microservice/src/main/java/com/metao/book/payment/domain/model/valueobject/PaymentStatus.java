package com.metao.book.payment.domain.model.valueobject;

import lombok.Getter;

/**
 * Payment status enumeration
 */
@Getter
public enum PaymentStatus {
    PENDING("Payment is pending processing"),
    SUCCESSFUL("Payment completed successfully"),
    FAILED("Payment failed"),
    CANCELLED("Payment was cancelled");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return this == SUCCESSFUL || this == FAILED || this == CANCELLED;
    }

    public boolean isSuccessful() {
        return this == SUCCESSFUL;
    }

    public boolean canBeRetried() {
        return this == FAILED;
    }

    public boolean canBeCancelled() {
        return this == PENDING;
    }
}
