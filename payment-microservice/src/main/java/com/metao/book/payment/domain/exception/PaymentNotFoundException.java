package com.metao.book.payment.domain.exception;

import com.metao.book.payment.domain.model.valueobject.PaymentId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a payment is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(PaymentId paymentId) {
        super("Payment id %s not found".formatted(paymentId));
    }
}
