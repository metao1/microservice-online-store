package com.metao.book.shared.application.service;

import java.util.function.BiConsumer;

public interface Binary<T, U> {

    void biAccept(BiConsumer<T,U> biConsumer);
}
