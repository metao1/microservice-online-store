package com.metao.book.order.application.port;

public interface ConsumedMessagePort {

    boolean claim(String consumerName, String eventId);
}
