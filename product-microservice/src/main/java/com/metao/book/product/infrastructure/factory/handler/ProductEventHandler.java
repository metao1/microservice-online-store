package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.event.CreateProductEvent;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProductEventHandler {

    private final Set<MessageHandler<CreateProductEvent>> messageHandlers = new HashSet<>();

    public void addMessageHandler(MessageHandler<CreateProductEvent> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void sendEvent(CreateProductEvent event) {
        messageHandlers.forEach(me -> me.onMessage(event));
    }
}
