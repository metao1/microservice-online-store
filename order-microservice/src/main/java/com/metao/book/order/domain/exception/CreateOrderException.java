package com.metao.book.order.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class CreateOrderException extends RuntimeException {

    public CreateOrderException() {
        super("Could not create order");
    }
}
