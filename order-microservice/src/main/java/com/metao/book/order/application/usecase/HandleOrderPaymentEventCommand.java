package com.metao.book.order.application.usecase;

import com.metao.book.order.domain.model.valueobject.PaymentStatus;
import com.metao.book.order.domain.exception.InvalidPaymentEventException;

public record HandleOrderPaymentEventCommand(
    String eventId,
    String orderId,
    PaymentStatus paymentStatus
) {

    public HandleOrderPaymentEventCommand {
        if (paymentStatus == null) {
            throw new InvalidPaymentEventException("Payment status must not be null");
        }
    }

    public HandleOrderPaymentEventCommand(String eventId, String orderId, String paymentStatus) {
        this(eventId, orderId, PaymentStatus.from(paymentStatus));
    }
}
