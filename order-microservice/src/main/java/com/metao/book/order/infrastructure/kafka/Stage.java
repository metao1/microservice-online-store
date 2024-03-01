package com.metao.book.order.infrastructure.kafka;

import java.util.function.BiFunction;
import java.util.function.Function;

interface Stage<T> {

    <U> Stage<U> thenApply(Function<? super T, ? extends U> function);

    <U> Stage<U> handleException(BiFunction<? super T, Throwable, ? extends U> function);
}