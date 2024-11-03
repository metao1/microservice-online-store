package com.metao.book.order.application.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;

@Getter
@Slf4j
@TestConfiguration
public class KafkaOrderConsumerTestConfig {

    /*private final CountDownLatch latch = new CountDownLatch(5);

    @RetryableTopic
    @KafkaListener(id = "order-listener-test", topics = "${kafka.topic.order-created.name}", containerFactory = "orderCreatedEventKafkaListenerContainerFactory", concurrency = "20")
    public void onEvent(ConsumerRecord<String, OrderCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }*/

}