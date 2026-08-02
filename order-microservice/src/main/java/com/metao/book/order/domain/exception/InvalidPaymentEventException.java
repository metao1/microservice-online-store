package com.metao.book.order.domain.exception;

public class InvalidPaymentEventException extends RuntimeException {

    public InvalidPaymentEventException(String message) {
        super(message);
    }

    public InvalidPaymentEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
