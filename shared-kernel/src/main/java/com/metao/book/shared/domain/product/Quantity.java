package com.metao.book.shared.domain.product;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Quantity value object used across services (inventory/order).
 * Allows zero but disallows negative amounts.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Quantity implements ValueObject {

    private final BigDecimal value;

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
        return new Quantity(this.value.add(other.value));
    }

    public Quantity subtract(@NonNull Quantity other) {
        BigDecimal newValue = this.value.subtract(other.value);
        if (newValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        return new Quantity(newValue);
    }

    @NotNull
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
