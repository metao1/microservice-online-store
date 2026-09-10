package com.metao.book.order.domain.exception;

public class ShoppingCartIsEmptyException extends RuntimeException {

    public ShoppingCartIsEmptyException() {
        super("Shopping cart is empty, cannot create an order.");
    }
}
