package com.metao.book.product.domain.service;

import com.metao.book.product.application.service.Joiner;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class OrderProductJoiner implements Joiner<Product, ProductCreatedEvent, ProductCreatedEvent> {

    @Override
    public ProductCreatedEvent join(Product input, ProductCreatedEvent output) {
        return null;
    }
}