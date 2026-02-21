package com.metao.book.order.domain.model.valueobject;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record OrderId(String value) {

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}