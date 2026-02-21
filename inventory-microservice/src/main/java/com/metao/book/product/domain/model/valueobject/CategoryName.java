package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.NonNull;

/**
 * Category name value object
 */
public record CategoryName(String value) implements ValueObject {

    public CategoryName(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Category name cannot exceed 100 characters");
        }
        this.value = value.trim().toLowerCase();
    }

    public static CategoryName of(String value) {
        return new CategoryName(value);
    }
}
