package com.metao.book.retails.infrustructure.factory.handler;

import com.metao.book.shared.domain.base.DomainEvent;

public abstract class MessageHandlerDecorator<T extends DomainEvent> implements MessageHandler<T> {

        private final MessageHandler<T> handler;

        MessageHandlerDecorator(MessageHandler<T> handler) {
                this.handler = handler;
        }

        @Override
        public void onMessage(T event) {
                this.handler.onMessage(event);
        }

}
