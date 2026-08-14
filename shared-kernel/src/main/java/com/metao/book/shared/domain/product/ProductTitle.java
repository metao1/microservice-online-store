package com.metao.book.shared.domain.product;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Product title value object
 */
@Embeddable
@NoArgsConstructor
public class ProductTitle implements ValueObject {

    private String value;

    public ProductTitle(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product title cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static ProductTitle of(String value) {
        return new ProductTitle(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductTitle that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}
