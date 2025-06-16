package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product title value object
 */
public record ProductTitle(String value) implements ValueObject {

    public ProductTitle(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product title cannot be null or empty");
        }
        if (value.length() < 3 || value.length() > 2500) {
            throw new IllegalArgumentException("Product title must be between 3 and 2500 characters");
        }
        this.value = value.trim();
    }

    public static ProductTitle of(String value) {
        return new ProductTitle(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}
