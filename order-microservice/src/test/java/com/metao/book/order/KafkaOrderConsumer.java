package com.metao.book.order;

import com.order.microservice.avro.OrderAvro;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Server
@EnableKafka
public class KafkaOrderConsumer {

    private CountDownLatch latch = new CountDownLatch(10);
    private String payload = null;

    @KafkaListener(id = "orders", topics = "order-test-2", groupId = "payment")
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