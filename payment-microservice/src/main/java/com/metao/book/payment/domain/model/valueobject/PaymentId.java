package com.metao.book.payment.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import java.util.UUID;
import lombok.NonNull;

/**
 * Payment identifier value object
 */
public record PaymentId(String value) implements ValueObject {

    public PaymentId(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("PaymentId cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    public static PaymentId of(String value) {
        return new PaymentId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
