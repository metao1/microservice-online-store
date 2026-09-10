package com.metao.book.payment.domain.exception;

import com.metao.book.payment.domain.model.valueobject.PaymentId;

/**
 * Exception thrown when a payment is not found
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(PaymentId paymentId) {
        super("Payment id %s not found".formatted(paymentId));
    }
}
