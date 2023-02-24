package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.domain.OrderId;
import com.metao.book.shared.domain.base.AbstractEntity;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An immutable obj helper class that is thread-safe
 */
public record OrderProcessorHelper<T, U>(U event) {

    public void ifPresentRun(
        Function<U, ? super AbstractEntity<OrderId>> biConsumer,
        BiConsumer<U, Object> consumer,
        Consumer<Exception> exceptionally
    ) {
        Objects.requireNonNull(biConsumer, "biConsumer can't be null");
        Objects.requireNonNull(exceptionally, "exceptionally can't be null");
        try {
            if (this.event != null) {
                final var call = biConsumer.apply(this.event);
                consumer.accept(this.event, call);
            } else {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            exceptionally.accept(e);
        }
    }
}