package com.metao.book.payment.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import com.metao.book.shared.architecture.DomainComponent;
import lombok.NonNull;

/**
 * Order identifier value object for payment context
 */
@DomainComponent
public record OrderId(String value) implements ValueObject {

    public OrderId(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    @NonNull
    public String toString() {
        return value;
    }
}
