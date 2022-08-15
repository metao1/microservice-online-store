package com.metao.book.retails.infrastructure.factory.handler;

public interface MessageHandler<T> {
    void onMessage(T event);
}