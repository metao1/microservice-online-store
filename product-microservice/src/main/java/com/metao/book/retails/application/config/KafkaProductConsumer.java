package com.metao.book.retails.application.config;

import com.metao.book.shared.GetProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Slf4j
@EnableKafka
@Component
public class KafkaProductConsumer {

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(id = "product-listener", topics = "product-request", groupId = "products-grp")
    public void onEvent(ConsumerRecord<String, GetProductEvent> record) {
        log.info("Consumed message -> {}", record.value());
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}