package com.metao.book.product.infrastructure.factory.handler.kafka;

import com.metao.book.shared.ProductEvent;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@EnableKafka
@TestConfiguration
public class KafkaProductConsumerTestConfig {

    private final CountDownLatch latch = new CountDownLatch(1);

    @Transactional
    @KafkaListener(id = "product-listener", topics = "product-topic", groupId = "products-grp")
    public void onEvent(ConsumerRecord<String, ProductEvent> record) {
        log.info("Consumed message -> {}", record.value());
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}