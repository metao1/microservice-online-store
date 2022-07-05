package com.metao.book.retails.infrustructure.factory.handler;

import com.metao.book.retails.domain.event.CreateProductEvent;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ProductEventHandler {

    private final Set<MessageHandler<CreateProductEvent>> messageHandlers = new HashSet<>();

    public void addMessageHandler(MessageHandler<CreateProductEvent> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void sendEvent(CreateProductEvent event) {
        messageHandlers
                .forEach(me -> me.onMessage(event));
    }
}
