package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product description value object
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductDescription implements ValueObject {

    private final String value;

    public ProductDescription(@NonNull String value) {
        if (value.length() > 10_485_760) { // 10MB limit
            throw new IllegalArgumentException("Product description exceeds 10MB limit");
        }
        this.value = value.trim();
    }

    public static ProductDescription of(String value) {
        return new ProductDescription(value);
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}