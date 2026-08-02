package com.metao.book.order.domain.exception;

public class ShoppingCartNotFoundException extends RuntimeException {

    public ShoppingCartNotFoundException(
        String message
    ) {
        super("Shopping cart not found: " + message);
    }
}
