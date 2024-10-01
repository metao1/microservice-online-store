package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.application.service.EventHandler;
import org.springframework.stereotype.Service;

@Service
public class ProductHandler extends EventHandler<ProductCreatedEvent> {

    @Override
    public ProductCreatedEvent getEvent() {
        return null;
    }
}
