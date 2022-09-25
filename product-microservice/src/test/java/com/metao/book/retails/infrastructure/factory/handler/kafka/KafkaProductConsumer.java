package com.metao.book.retails.infrastructure.factory.handler.kafka;

import com.metao.book.shared.GetProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

@Slf4j
@EnableKafka
@TestConfiguration
public class KafkaProductConsumer {

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(id = "product-listener", topics = "get-product-request", groupId = "products-grp")
    public void onEvent(ConsumerRecord<String, GetProductEvent> record) {
        log.info("Consumed message -> {}", record.value());
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}