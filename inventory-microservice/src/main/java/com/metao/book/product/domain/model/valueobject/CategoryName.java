package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Category name value object
 */
@Getter
@EqualsAndHashCode
public class CategoryName implements ValueObject {

    private final String value;

    public CategoryName(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Category name cannot exceed 100 characters");
        }
        this.value = value.trim();
    }

    public static CategoryName of(String value) {
        return new CategoryName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
