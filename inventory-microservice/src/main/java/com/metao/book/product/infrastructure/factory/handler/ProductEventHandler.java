package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.event.ProductCreatedEvent;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProductEventHandler {

    private final Set<MessageHandler<ProductCreatedEvent>> messageHandlers = new HashSet<>();

    public void addMessageHandler(MessageHandler<ProductCreatedEvent> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void publish(ProductCreatedEvent event) {
        messageHandlers.forEach(me -> me.onMessage(event));
    }
}
