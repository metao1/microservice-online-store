package com.metao.product.infrustructure.factory.handler;

import com.metao.ddd.base.DomainEvent;

public class KafkaMessageHandler extends EventMessageHandler {

        public KafkaMessageHandler(MessageHandler<DomainEvent> handler) {
                super(handler);
        }

        public void sendMessage(DomainEvent event){
                super.onMessage(event);

        }

        
        
}
