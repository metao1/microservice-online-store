package com.metao.book.shared.domain.product;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Product identifier value object
 */
@Embeddable
@NoArgsConstructor
public class ProductSku implements ValueObject {

    private String value;
    public ProductSku(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("sku cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static ProductSku generate() {
        // Generate a 10-character SKU-like identifier
        return ProductSku.of(UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
    }

    public static ProductSku of(String value) {
        return new ProductSku(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductSku that)) {
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
