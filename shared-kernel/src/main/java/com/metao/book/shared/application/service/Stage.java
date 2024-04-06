package com.metao.book.shared.application.service;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stage<T> {

    <U> Stage<U> map(Function<? super T, ? extends U> function);

    <U> U applyExceptionally(BiFunction<? super T, Throwable, ? extends U> function);

    void acceptExceptionally(BiConsumer<? super T, Throwable> biConsumer);
}