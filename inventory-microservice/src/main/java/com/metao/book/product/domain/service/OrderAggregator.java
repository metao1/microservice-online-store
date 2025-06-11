package com.metao.book.product.domain.service;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.application.service.Aggregator;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderAggregator implements Aggregator<ProductCreatedEvent, BigDecimal> {

    @Override
    public BigDecimal apply(ProductCreatedEvent order, BigDecimal total) {
        return total.add(BigDecimal.valueOf(order.getPrice()));
    }
}
