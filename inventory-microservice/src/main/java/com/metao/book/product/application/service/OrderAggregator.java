package com.metao.book.product.application.service;

import com.metao.book.shared.domain.order.OrderEvent;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderAggregator implements Aggregator<OrderEvent, BigDecimal> {

    @Override
    public BigDecimal apply(OrderEvent order, BigDecimal total) {
        return total.add(order.quantity());
    }

}
