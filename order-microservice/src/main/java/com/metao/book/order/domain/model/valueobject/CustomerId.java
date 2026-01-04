package com.metao.book.order.domain.model.valueobject;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CustomerId {

    String value;

    // Default constructor for Hibernate
    public CustomerId() {
    }

    public CustomerId(String value) {
        this.value = value;
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}