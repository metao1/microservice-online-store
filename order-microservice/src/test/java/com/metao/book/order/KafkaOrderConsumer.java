package com.metao.book.order;

import java.util.concurrent.CountDownLatch;

import com.metao.book.order.application.dto.OrderDTO;

import org.springframework.kafka.annotation.KafkaListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaOrderConsumer {

    private CountDownLatch latch = new CountDownLatch(1);
    private String payload = null;

    @KafkaListener(id = "orders", topics = "order", groupId = "payment")
    public void onEvent(OrderDTO o) {
        log.info("Received: {}", o);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public String getPayload() {
        return payload;
    }
}