package com.metao.product.infrustructure.factory.handler;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.event.CreateProductEvent;

import org.springframework.stereotype.Service;

@Service
public class ProductEventHandler {

    private final Set<MessageHandler<CreateProductEvent>> messageHandlers = new HashSet<>();

    public void addMessageHandler(MessageHandler<CreateProductEvent> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void sendEvent(CreateProductEvent event) {
        messageHandlers.forEach(mh -> mh.onMessage(event));
    }

    public CreateProductEvent createEvent(ProductDTO productDTO) {
        return new CreateProductEvent(productDTO, Instant.now(), Instant.now());
    }

}
