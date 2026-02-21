package com.metao.book.order.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.OrderUpdatedEvent.Status;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.KafkaContainer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class OrderEventHandlerContainerIT extends KafkaContainer {

    private CountDownLatch latch1;
    private CountDownLatch latch2;

    @Autowired
    KafkaEventHandler eventHandler;

    @Autowired
    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate1;

    @Autowired
    KafkaTemplate<String, OrderUpdatedEvent> kafkaTemplate2;

    @Autowired
    KafkaListenerEndpointRegistry registry;

    @BeforeAll
    void waitForKafkaAssignment() {
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @BeforeEach
    void resetLatches() {
        latch1 = new CountDownLatch(1);
        latch2 = new CountDownLatch(1);
    }

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

        var topic = eventHandler.getKafkaTopic(event.getClass());

        kafkaTemplate1.send(topic, event.getId(), event).get(10, TimeUnit.SECONDS);
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> assertThat(latch1.getCount()).isZero());
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

        kafkaTemplate1.send(eventHandler.getKafkaTopic(event1.getClass()), event1.getId(), event1)
            .get(10, TimeUnit.SECONDS);
        kafkaTemplate2.send(eventHandler.getKafkaTopic(event2.getClass()), event2.getId(), event2)
            .get(10, TimeUnit.SECONDS);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(latch1.getCount()).isZero();
            assertThat(latch2.getCount()).isZero();
        });

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
