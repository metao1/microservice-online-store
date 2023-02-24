package com.metao.book.order.kafka;

import com.metao.book.order.application.service.OrderValidator;
import com.metao.book.shared.OrderEvent;
import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@EnableKafka
@TestConfiguration
@RequiredArgsConstructor
public class KafkaOrderConsumerTestConfig {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final OrderValidator orderValidator;

    @KafkaListener(
        id = "${kafka.topic.order}-test-id",
        topics = "order-topic-test",
        groupId = "${kafka.topic.order}" + "-grp-test"
    )
    public void onEvent(ConsumerRecord<String, OrderEvent> record) {
        log.info("Consumed message -> {}", record);
        orderValidator.validate(record.value());
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}