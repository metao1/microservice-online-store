package com.metao.product.infrustructure.factory.handler;

import com.metao.ddd.base.DomainEvent;

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
