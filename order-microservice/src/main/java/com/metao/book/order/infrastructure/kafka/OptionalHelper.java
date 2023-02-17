package com.metao.book.order.infrastructure.kafka;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An immutable obj that is thread-safe
 */
public record OptionalHelper<T>(T value) {

    public void ifPresentRun(
        Supplier<T> positive,
        Consumer<Exception> exceptionally,
        Consumer<T> runnable
    ) {
        Objects.requireNonNull(runnable, "runnable can't be null");
        try {
            if (this.value != null) {
                runnable.accept(positive.get());
            } else {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            exceptionally.accept(e);
        }
    }
}