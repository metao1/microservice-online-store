package com.metao.product.infrustructure.factory.handler;

import com.metao.ddd.base.DomainEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventMessageHandler extends MessageHandlerDecorator<DomainEvent> {
        
        public EventMessageHandler(MessageHandler<DomainEvent> handler) {
                super(handler);
        }

        @Override
        public void onMessage(DomainEvent event) {
                super.onMessage(event);
                log.info("event occured on: {}", event.occurredOn());
        }

}
