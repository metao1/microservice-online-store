package com.metao.book.order.application.dto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FindOrderException extends RuntimeException {

    public FindOrderException() {
        super("Could not find order");
    }
}
