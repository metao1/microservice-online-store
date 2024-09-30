package com.metao.book.product.domain.service;

import com.metao.book.product.application.service.Joiner;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.domain.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderProductJoiner implements Joiner<ProductEntity, ProductCreatedEvent, ProductCreatedEvent> {

    @Override
    public ProductCreatedEvent join(ProductEntity input, ProductCreatedEvent output) {
        return null;
    }
}