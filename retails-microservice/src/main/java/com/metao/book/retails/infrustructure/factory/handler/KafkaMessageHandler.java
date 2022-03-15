package com.metao.book.retails.infrustructure.factory.handler;

import com.metao.book.shared.domain.base.DomainEvent;

public class KafkaMessageHandler extends EventMessageHandler {

        public KafkaMessageHandler(MessageHandler<DomainEvent> handler) {
                super(handler);
        }

        public void sendMessage(DomainEvent event) {
                super.onMessage(event);
        }

}
