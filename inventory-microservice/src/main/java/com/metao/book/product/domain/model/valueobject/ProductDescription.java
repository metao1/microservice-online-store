package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product description value object
 */
public record ProductDescription(String value) implements ValueObject {

    public ProductDescription(@NonNull String value) {
        if (value.length() > 10_485_760) { // 10MB limit
            throw new IllegalArgumentException("Product description exceeds 10MB limit");
        }
        this.value = value.trim();
    }

    public static ProductDescription of(String value) {
        return new ProductDescription(value);
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}
