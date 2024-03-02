package com.metao.book.order.infrastructure.kafka;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An immutable OrderProcessorHelper class that is thread-safe
 */
public class StageProcessor<T> implements Stage<T> {

    private final T event;
    private Throwable cause;

    private StageProcessor(T event) {
        this.event = event;
    }

    static <U> Stage<U> process(U event) {
        return new MinimalStage<>(event);
    }

    @Override
    public <U> StageProcessor<U> thenApply(Function<? super T, ? extends U> function) {
        try {
            U res = function.apply(event);
            return new MinimalStage<>(res);
        } catch (Exception e) {
            this.cause = e;
        }
        return new MinimalStage<>(null);
    }

    @Override
    public <U> StageProcessor<U> handleException(BiFunction<? super T, Throwable, ? extends U> function) {
        try {
            U res = function.apply(event, cause);
            return new MinimalStage<>(res);
        } catch (Exception e) {
            this.cause = e;
        }
        return new MinimalStage<>(null);
    }

    @Override
    public void acceptException(BiConsumer<? super T, Throwable> biConsumer) {
        if (cause != null) {
            biConsumer.accept(event, cause);
        }
    }

    private static final class MinimalStage<T> extends StageProcessor<T> {

        public MinimalStage(T event) {
            super(event);
        }
    }

}