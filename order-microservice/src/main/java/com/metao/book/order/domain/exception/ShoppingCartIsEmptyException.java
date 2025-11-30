package com.metao.book.order.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShoppingCartIsEmptyException extends RuntimeException {

    public ShoppingCartIsEmptyException() {
        super("Shopping cart is empty, cannot create an order.");
    }
}
