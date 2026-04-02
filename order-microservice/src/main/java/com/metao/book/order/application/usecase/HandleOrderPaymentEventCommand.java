package com.metao.book.order.application.usecase;

public record HandleOrderPaymentEventCommand(
    String eventId,
    String orderId,
    String paymentStatus
) {
}
