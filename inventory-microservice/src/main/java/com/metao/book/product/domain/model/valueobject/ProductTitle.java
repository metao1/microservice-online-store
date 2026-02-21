package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.validation.constraints.NotNull;

/**
 * Product title value object
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductTitle implements ValueObject {

    private final String value;

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