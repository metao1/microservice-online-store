package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Category identifier value object
 */
public record CategoryId(Long value) implements ValueObject {

    public CategoryId(@NonNull Long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("CategoryId must be a positive number");
        }
        this.value = value;
    }

    public static CategoryId of(Long value) {
        return new CategoryId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }
}
