package com.metao.book.order.domain.model.valueobject;

import lombok.Value;

@Value
public class CustomerId {

    String value;

    public CustomerId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        this.value = value;
    }

    // Default constructor for Hibernate
    public CustomerId() {
        // ignore
        this.value = null;
    }

    @Override
    public String toString() {
        return value;
    }
}