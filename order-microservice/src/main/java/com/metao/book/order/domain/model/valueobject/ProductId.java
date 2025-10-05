package com.metao.book.order.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.Value;

@Value
public class ProductId implements ValueObject {

    String value;

    public ProductId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        this.value = value;
    }

    // Default constructor for Hibernate
    public ProductId() {
        this.value = null;
    }

    @Override
    public String toString() {
        return value;
    }
}