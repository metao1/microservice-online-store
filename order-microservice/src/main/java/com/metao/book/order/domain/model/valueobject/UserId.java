package com.metao.book.order.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record UserId(String value) {

    public static UserId of(String value) {
        return new UserId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}