package com.metao.product.infrustructure.factory.handler;

public interface MessageHandler<T> {
        void onMessage(T event);
}