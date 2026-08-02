package com.metao.book.payment.application.port;

public interface ConsumedMessagePort {

    boolean claim(String consumerName, String eventId);
}
