package com.metao.book.product.application.service;

import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.shared.domain.order.OrderUpdateEvent;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderProductJoiner implements Joiner<ProductEvent, OrderUpdateEvent, OrderUpdateEvent> {

    @Override
    public OrderUpdateEvent join(ProductEvent input, OrderUpdateEvent output) {
        return null;
    }

    private BigDecimal calculateAvailableItems(BigDecimal totalOrderQuantity, BigDecimal totalProductCount) {
        return totalProductCount.subtract(totalOrderQuantity);
    }
}