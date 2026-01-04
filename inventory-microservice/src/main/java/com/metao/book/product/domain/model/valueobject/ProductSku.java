package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product identifier value object
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductSku implements ValueObject {

    private final String value;

    public ProductSku(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("ProductId cannot be null or empty");
        }
        if (value.length() != 10) {
            throw new IllegalArgumentException("ProductId must be exactly 10 characters (SKU format)");
        }
        this.value = value.trim();
    }

    public static ProductSku generate() {
        // Generate a 10-character SKU-like identifier
        return new ProductSku(UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
    }

    public static ProductSku of(String value) {
        return new ProductSku(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}