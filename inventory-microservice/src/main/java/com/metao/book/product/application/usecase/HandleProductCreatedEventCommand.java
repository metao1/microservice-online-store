package com.metao.book.product.application.usecase;

public record HandleProductCreatedEventCommand(
    String eventId,
    String sku
) {
}
