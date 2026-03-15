package com.metao.book.shared.domain.product;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.NonNull;

/**
 * Quantity value object used across services (inventory/order).
 * Allows zero but disallows negative amounts.
 */
@Embeddable
public record Quantity(BigDecimal value) implements ValueObject {

    public Quantity(@NonNull BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.value = value;
    }

    public static Quantity of(BigDecimal value) {
        return new Quantity(value);
    }

    public Quantity add(@NonNull Quantity other) {
        return Quantity.of(this.value.add(other.value));
    }

    public Quantity subtract(@NonNull Quantity other) {
        BigDecimal newValue = this.value.subtract(other.value);
        if (newValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        return Quantity.of(newValue);
    }

    @NotNull
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
