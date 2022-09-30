package com.metao.book.order;

import com.metao.book.shared.OrderAvro;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Value
@EnableKafka
public class KafkaOrderConsumer {

    CountDownLatch latch = new CountDownLatch(10);
    String payload = null;

    @KafkaListener(id = "orders", topics = "order-test", groupId = "payment")
    public void onEvent(OrderAvro record) {
        log.info("Consumed message -> {}", record);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public String getPayload() {
        return payload;
    }
}