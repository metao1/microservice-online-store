package com.metao.book.product.infrastructure.factory.handler.kafka;

import com.metao.book.product.event.ProductCreatedEvent;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

@Getter
@Slf4j
@TestConfiguration
//@ImportAutoConfiguration(KafkaTransactionTestConfiguration.class)
public class KafkaProductConsumerTestConfig {

    private final CountDownLatch latch = new CountDownLatch(10);

    @RetryableTopic
    @KafkaListener(id = "product-listener-test", topics = "${kafka.topic.product.name}", containerFactory = "productCreatedEventKafkaListenerContainerFactory", concurrency = "20")
    public void onEvent(ConsumerRecord<String, ProductCreatedEvent> consumerRecord) {

        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }

}