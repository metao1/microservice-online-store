package com.metao.book.retails.application.config;

import com.metao.book.shared.domain.RemoteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Slf4j
@EnableKafka
@Component
public class KafkaProductConsumer {

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(id = "product", topics = "products", groupId = "products")
    public void onEvent(RemoteEvent record) {
        log.info("Consumed message -> {}", record);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}