package com.metao.book.retails.infrustructure.factory.handler;

public abstract class EventHandler<T> {

        protected abstract void addMessageHandler(T messageHandler);

}
