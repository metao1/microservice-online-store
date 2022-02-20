package com.metao.product.infrustructure.factory;

import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.event.CreateProductEvent;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class EventHandler {
    private final Set<MessageHandler> messageHandlers = new HashSet<>();

    public void sendEvent(CreateProductEvent event) {
        messageHandlers.forEach(mh -> mh.onCreateProductEvent(event));
    }

    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public CreateProductEvent createEvent(ProductDTO productDTO) {
        return new CreateProductEvent(productDTO, Instant.now(), Instant.now());
    }

}
