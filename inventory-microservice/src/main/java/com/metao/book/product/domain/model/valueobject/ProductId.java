package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import java.util.UUID;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product identifier value object
 */
public record ProductId(String value) implements ValueObject {

    public ProductId(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("ProductId cannot be null or empty");
        }
        if (value.length() != 10) {
            throw new IllegalArgumentException("ProductId must be exactly 10 characters (ASIN format)");
        }
        this.value = value.trim();
    }

    public static ProductId generate() {
        // Generate a 10-character ASIN-like identifier
        return new ProductId(UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}
