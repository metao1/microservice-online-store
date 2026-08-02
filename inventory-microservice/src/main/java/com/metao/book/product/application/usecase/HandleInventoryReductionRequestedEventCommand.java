package com.metao.book.product.application.usecase;

import java.math.BigDecimal;

public record HandleInventoryReductionRequestedEventCommand(
    String eventId,
    String orderId,
    String sku,
    BigDecimal quantity
) {
}
