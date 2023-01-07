package com.metao.book.product.application.service;

import com.metao.book.shared.OrderEvent;
import org.apache.kafka.streams.kstream.Aggregator;
import org.springframework.stereotype.Component;

@Component
public class OrderAggregator implements Aggregator<String, OrderEvent, Double> {

    @Override
    public Double apply(String key, OrderEvent order, Double total) {
        return total + order.getQuantity();
    }

}
