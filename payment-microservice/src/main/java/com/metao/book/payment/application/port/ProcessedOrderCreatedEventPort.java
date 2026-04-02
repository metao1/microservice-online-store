package com.metao.book.payment.application.port;

public interface ProcessedOrderCreatedEventPort {

    boolean markProcessed(String eventId);
}
