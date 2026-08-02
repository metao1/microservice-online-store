package com.metao.book.payment.domain.exception;


/**
 * Exception thrown when attempting to create a duplicate payment
 */
public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String message) {
        super(message);
    }
}
