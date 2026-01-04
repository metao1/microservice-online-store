package com.metao.book.order.domain.exception;

import com.metao.book.order.domain.model.valueobject.OrderId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId orderId) {
        super("order not found with id: " + orderId);
    }
}
