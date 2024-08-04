package com.metao.book.product.infrastructure.factory.handler.kafka;

import com.metao.book.product.domain.event.ProductCreatedEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

import java.util.concurrent.CountDownLatch;

@Getter
@Slf4j
@EnableKafka
@TestConfiguration
@ImportAutoConfiguration(KafkaTransactionTestConfiguration.class)
public class KafkaProductConsumerTestConfig {

    private final CountDownLatch latch = new CountDownLatch(1);

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "product-listener", topics = "${kafka.topic.product}")
    public void onEvent(ConsumerRecord<String, ProductCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.value());
        latch.countDown();
    }

}