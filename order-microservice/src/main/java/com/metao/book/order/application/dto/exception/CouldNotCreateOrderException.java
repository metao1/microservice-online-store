package com.metao.book.order.application.dto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class CouldNotCreateOrderException extends RuntimeException {

    public CouldNotCreateOrderException() {
        super("Could not create order");
    }

    public CouldNotCreateOrderException(Exception e) {
        super(e);
    }
}
