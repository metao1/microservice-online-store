package com.metao.book.cart.service.mapper;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class CartMapper<E, T> {

    public T mapToEvent(
        Supplier<E> supplier,
        Predicate<E> validator
    ) {
        Objects.requireNonNull(validator, "validator should not be null");
        Objects.requireNonNull(supplier, "supplier should not be null");
        var item = supplier.get();
        if (validator.test(item)) {
            return apply(item);
        } else {
            throw new RuntimeException("can't validate: " + item);
        }
    }

    protected abstract T apply(E item);

}
