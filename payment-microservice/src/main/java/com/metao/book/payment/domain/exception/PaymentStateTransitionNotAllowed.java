package com.metao.book.payment.domain.exception;

import com.metao.book.payment.domain.model.valueobject.PaymentStatus;

public class PaymentStateTransitionNotAllowed extends IllegalStateException {

    public PaymentStateTransitionNotAllowed(PaymentStatus current, PaymentStatus target) {
        super("Cannot transition payment from " + current + " to " + target);
    }
}
