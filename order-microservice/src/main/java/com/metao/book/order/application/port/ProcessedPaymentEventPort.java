package com.metao.book.order.application.port;

public interface ProcessedPaymentEventPort {

    boolean markProcessed(String eventId);
}
