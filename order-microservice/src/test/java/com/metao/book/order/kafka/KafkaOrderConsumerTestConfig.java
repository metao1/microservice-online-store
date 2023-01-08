package com.metao.book.order.kafka;

import com.metao.book.shared.OrderEvent;

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
public class KafkaOrderConsumerTestConfig {

    private final CountDownLatch latch = new CountDownLatch(10);

    @KafkaListener(id = "order-id", topics = "${kafka.topic.order}", groupId = "order-grp-id-test")
    public void onEvent(ConsumerRecord<String, OrderEvent> record) {
        log.info("Consumed message -> {}", record);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}