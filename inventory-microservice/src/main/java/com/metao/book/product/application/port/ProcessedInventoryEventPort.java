package com.metao.book.product.application.port;

public interface ProcessedInventoryEventPort {

    boolean markProcessed(String eventId);
}
