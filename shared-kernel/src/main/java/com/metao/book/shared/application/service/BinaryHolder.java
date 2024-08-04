package com.metao.book.shared.application.service;

import java.util.function.BiConsumer;

public class BinaryHolder<T, U> {

    private T prev;
    private U next;
    private static BinaryHolder INSTANCE;

    private BinaryHolder(T prev) {
        this.prev = prev;
    }

    public synchronized static <T, U> BinaryHolder<T, U> accept(T val) {
        INSTANCE = new BinaryHolder(val);
        return INSTANCE;
    }

    public synchronized static <T, U> BinaryHolder<T, U> next(U val) {
        INSTANCE.next = val;
        return INSTANCE;
    }

    public <T, U> void biAccept(BiConsumer<T, U> biConsumer) {
        biConsumer.accept((T) INSTANCE.prev, (U) INSTANCE.next);
    }
}
