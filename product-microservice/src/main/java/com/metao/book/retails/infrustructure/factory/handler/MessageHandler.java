package com.metao.book.retails.infrustructure.factory.handler;

public interface MessageHandler<T> {
    void onMessage(T event);
}