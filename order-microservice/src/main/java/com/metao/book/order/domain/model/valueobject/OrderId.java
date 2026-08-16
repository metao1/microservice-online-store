package com.metao.book.order.domain.model.valueobject;

import com.metao.book.shared.architecture.DomainComponent;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

@DomainComponent
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
