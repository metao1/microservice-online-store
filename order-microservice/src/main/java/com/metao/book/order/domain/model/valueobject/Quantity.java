package com.metao.book.order.domain.model.valueobject;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class Quantity {

    int value;

    public Quantity(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.value = value;
    }

    // Default constructor for Hibernate
    public Quantity() {
        this.value = 1; // Default to 1 to avoid validation issues
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        int newValue = this.value - other.value;
        if (newValue <= 0) {
            throw new IllegalArgumentException("Resulting quantity must be positive");
        }
        return new Quantity(newValue);
    }

    @NotNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}