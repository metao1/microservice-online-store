package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Category identifier value object
 */
public record CategoryId(String value) implements ValueObject {

    public CategoryId(@NotBlank String value) {
        this.value = value;
    }

    public static CategoryId of(String value) {
        return new CategoryId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}
