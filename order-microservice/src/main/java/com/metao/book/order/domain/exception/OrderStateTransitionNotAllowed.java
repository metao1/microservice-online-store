package com.metao.book.order.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class OrderStateTransitionNotAllowed extends RuntimeException {

    public OrderStateTransitionNotAllowed(String message) {
        super("Transition error: " + message);
    }
}
