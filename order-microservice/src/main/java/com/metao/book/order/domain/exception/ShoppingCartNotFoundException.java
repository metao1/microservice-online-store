package com.metao.book.order.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShoppingCartNotFoundException extends RuntimeException {

    public ShoppingCartNotFoundException(
        String message
    ) {
        super("Shopping cart not found: " + message);
    }
}
