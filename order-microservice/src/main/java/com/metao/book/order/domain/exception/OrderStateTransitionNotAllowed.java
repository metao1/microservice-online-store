package com.metao.book.order.domain.exception;

public class OrderStateTransitionNotAllowed extends RuntimeException {

    public OrderStateTransitionNotAllowed(String message) {
        super("Transition error: " + message);
    }
}
