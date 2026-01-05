package com.metao.book.order.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
public class Quantity implements ValueObject {

    private BigDecimal value;

    public Quantity() {
    }

    public Quantity(BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.value = value;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value.add(other.value));
    }

    public Quantity subtract(Quantity other) {
        var newValue = this.value.subtract(other.value);
        if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Resulting quantity must be positive");
        }
        return new Quantity(newValue);
    }

    @NotNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}