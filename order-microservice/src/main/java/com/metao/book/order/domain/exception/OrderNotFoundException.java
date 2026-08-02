package com.metao.book.order.domain.exception;

import com.metao.book.order.domain.model.valueobject.OrderId;
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId orderId) {
        super("order not found with id: " + orderId);
    }
}
