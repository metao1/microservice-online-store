package com.metao.book.shared.application.service;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An immutable StageProcessor class that is thread-safe
 */
public class StageProcessor<T> implements Stage<T> {

    private final T value;
    private final Throwable cause;

    private StageProcessor(T value, Throwable cause) {
        this.value = value;
        this.cause = cause;
    }

    public static <U> Stage<U> accept(U event) {
        return new MinimalStage<>(event, null);
    }

    @Override
    public <U> Stage<U> map(Function<? super T, ? extends U> function) {
        try {
            if (cause != null) {
                throw new RuntimeException(cause);
            }
            U res = function.apply(value);
            return new MinimalStage<>(res, null);
        } catch (Exception e) {
            return new MinimalStage<>(null, e);
        }
    }

    public <U> U applyExceptionally(BiFunction<? super T, Throwable, ? extends U> function) {
        try {
            return function.apply(value, cause);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void acceptExceptionally(BiConsumer<? super T, Throwable> biConsumer) {
        biConsumer.accept(value, cause);
    }

    private static final class MinimalStage<T> extends StageProcessor<T> {

        public MinimalStage(T event, Exception exception) {
            super(event, exception);
        }
    }

}