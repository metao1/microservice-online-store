package com.metao.book.shared.domain.product;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

/**
 * Product title value object
 */
@Embeddable
public record ProductTitle(String value) implements ValueObject {

    public ProductTitle(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product title cannot be null or empty");
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