package com.metao.book.order.kafka;

import com.metao.book.order.application.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.CountDownLatch;

@Slf4j
@EnableKafka
@TestConfiguration
public class KafkaOrderConsumerTestConfig {

    private final CountDownLatch latch = new CountDownLatch(10);

    @Transactional
    @KafkaListener(id = "order-id", topics = "book-order-topic", groupId = "order-grp-id")
    public void onEvent(ConsumerRecord<String, OrderDTO> record) {
        log.info("Consumed message -> {}", record);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}