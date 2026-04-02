package com.metao.book.order.application.port;

public interface ProcessedOrderCreatedEventPort {

    boolean markProcessed(String eventId);
}
