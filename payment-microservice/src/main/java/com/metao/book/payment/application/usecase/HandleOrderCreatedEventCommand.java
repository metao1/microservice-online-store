package com.metao.book.payment.application.usecase;

import java.math.BigDecimal;

public record HandleOrderCreatedEventCommand(
    String eventId,
    String orderId,
    BigDecimal amount,
    String currency
) {
}
