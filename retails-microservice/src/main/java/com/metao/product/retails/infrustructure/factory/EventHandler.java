package com.metao.product.retails.infrustructure.factory;

import com.metao.product.retails.application.dto.ProductDTO;
import com.metao.product.retails.domain.product.event.CreateProductEvent;

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
