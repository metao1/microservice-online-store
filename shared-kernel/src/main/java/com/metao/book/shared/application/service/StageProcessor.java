package com.metao.book.shared.application.service;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An immutable OrderProcessorHelper class that is thread-safe
 */

public class StageProcessor<T> implements Stage<T> {

    private final T event;
    private final Throwable cause;

    private StageProcessor(T event, Throwable cause) {
        this.event = event;
        this.cause = cause;
    }

    public static <U> Stage<U> accept(U event) {
        return new MinimalStage<>(event, null);
    }

    @Override
    public <U> StageProcessor<U> thenApply(Function<? super T, ? extends U> function) {
        try {
            U res = function.apply(event);
            return new MinimalStage<>(res, null);
        } catch (Exception e) {
            return new MinimalStage<>(null, e);
        }
    }

    public <U> U handleExceptionally(BiFunction<? super T, Throwable, ? extends U> function) {
        try {
            return function.apply(event, cause);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void acceptExceptionally(BiConsumer<? super T, Throwable> biConsumer) {
        biConsumer.accept(event, cause);
    }

    private static final class MinimalStage<T> extends StageProcessor<T> {

        public MinimalStage(T event, Exception exception) {
            super(event, exception);
        }
    }

}