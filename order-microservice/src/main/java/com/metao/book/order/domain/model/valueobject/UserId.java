package com.metao.book.order.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
public record UserId(String value) {

    public static UserId of(String value) {
        Objects.requireNonNull(value, "userId can't be null or empty");
        return new UserId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}