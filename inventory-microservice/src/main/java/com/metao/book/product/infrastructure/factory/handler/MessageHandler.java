package com.metao.book.product.infrastructure.factory.handler;

public interface MessageHandler<T> {

    void onMessage(T event);
}