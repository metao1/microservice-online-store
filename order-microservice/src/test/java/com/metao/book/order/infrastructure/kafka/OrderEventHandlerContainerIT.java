package com.metao.book.order.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.OrderUpdatedEvent.Status;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.KafkaContainer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class OrderEventHandlerContainerIT extends KafkaContainer {

    private final CountDownLatch latch1 = new CountDownLatch(1);
    private final CountDownLatch latch2 = new CountDownLatch(1);

    @Autowired
    KafkaEventHandler eventHandler;

    @Test
    @SneakyThrows
    @DisplayName("When sending event then Kafka messages sent successfully")
    void handleSendEventThenKafkaMessagesSentSuccessfully() {
        var event = OrderCreatedEvent.newBuilder()
            .setStatus(OrderCreatedEvent.Status.NEW)
            .setId(OrderCreatedEvent.UUID.getDefaultInstance().toString())
            .setProductId("PRODUCT_ID")
            .setCustomerId("CUSTOMER_ID")
            .setQuantity(100d)
            .setPrice(100d)
            .setCurrency("USD")
            .build();

        eventHandler.send(event.getId(), event);
        latch1.await(10, TimeUnit.SECONDS);
        assertThat(latch1.getCount()).isZero();
    }

    @Test
    @SneakyThrows
    @DisplayName("When receiving event then Kafka messages processed successfully")
    void handleSendDifferentEventsThenKafkaMessagesSentSuccessfully() {
        var event1 = OrderCreatedEvent.newBuilder()
            .setStatus(OrderCreatedEvent.Status.NEW)
            .setId(OrderCreatedEvent.UUID.getDefaultInstance().toString())
            .setProductId("PRODUCT_ID")
            .setCustomerId("CUSTOMER_ID")
            .setQuantity(100d)
            .setPrice(100d)
            .setCurrency("USD")
            .build();

        var event2 = OrderUpdatedEvent.newBuilder()
            .setId(OrderUpdatedEvent.UUID.getDefaultInstance().toString())
            .setQuantity(100d)
            .setPrice(100d)
            .setCurrency("USD")
            .setStatus(Status.CONFIRMED)
            .build();

        eventHandler.send(event1.getId(), event1);
        eventHandler.send(event2.getId(), event2);
        latch1.await(10, TimeUnit.SECONDS);
        latch2.await(10, TimeUnit.SECONDS);

        assertThat(latch1.getCount()).isZero();
        assertThat(latch2.getCount()).isZero();
    }

    @RetryableTopic
    @KafkaListener(id = "order-updated-test-id", topics = "order-updated")
    public void onOrderUpdatedEvent(ConsumerRecord<String, OrderUpdatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.value());
        latch2.countDown();
    }

    @RetryableTopic
    @KafkaListener(id = "order-created-test-id", topics = "order-created")
    public void onOrderCreatedEvent(ConsumerRecord<String, OrderCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.value());
        latch1.countDown();
    }

}