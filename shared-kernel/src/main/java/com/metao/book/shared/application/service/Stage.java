package com.metao.book.shared.application.service;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stage<T> {

    <U> Stage<U> thenApply(Function<? super T, ? extends U> function);

    <U> U handleExceptionally(BiFunction<? super T, Throwable, ? extends U> function);

    void acceptExceptionally(BiConsumer<? super T, Throwable> consumer);
}