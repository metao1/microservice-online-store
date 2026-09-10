package com.metao.book.order.domain.model.valueobject;

import com.metao.book.shared.architecture.DomainComponent;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@DomainComponent
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
