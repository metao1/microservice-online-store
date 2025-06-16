package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import java.math.BigDecimal;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product volume/quantity value object
 */
public record ProductVolume(BigDecimal value) implements ValueObject {

    public ProductVolume(@NonNull BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product volume cannot be negative");
        }
        this.value = value;
    }

    public static ProductVolume of(BigDecimal value) {
        return new ProductVolume(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }
}
