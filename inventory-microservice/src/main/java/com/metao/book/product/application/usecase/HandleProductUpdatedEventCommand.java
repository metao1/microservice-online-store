package com.metao.book.product.application.usecase;

import java.math.BigDecimal;

public record HandleProductUpdatedEventCommand(
    String eventId,
    String sku,
    String description,
    BigDecimal volume
) {
}
