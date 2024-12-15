package com.metao.book.order.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.shared.CategoryOuterClass.Category;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.book.shared.application.kafka.EventConfiguration;
import com.metao.book.shared.application.kafka.OrderEventHandler;
import com.metao.shared.test.BaseKafkaIT;
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
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.test.context.TestPropertySource;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource(properties = "kafka.isEnabled=true")
@Import({OrderEventHandler.class, EventConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderEventHandlerTest extends BaseKafkaIT {

    private final CountDownLatch latch1 = new CountDownLatch(1);
    private final CountDownLatch latch2 = new CountDownLatch(1);

    @Autowired
    OrderEventHandler eventHandler;

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

        eventHandler.handle(event.getId(), event);
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

        var event2 = ProductUpdatedEvent.newBuilder()
            .setImageUrl("https://imageurl.com/")
            .setTitle("Product")
            .setDescription("Product Description")
            .setCurrency("USD")
            .setPrice(100d)
            .setVolume(100d)
            .addCategories(Category.newBuilder().setName("Category").build())
            .build();

        eventHandler.handle(event1.getId(), event1);
        eventHandler.handle(event2.getAsin(), event2);
        latch1.await(10, TimeUnit.SECONDS);
        latch2.await(10, TimeUnit.SECONDS);
    }

    @RetryableTopic
    @KafkaListener(id = "product-updated-test-id", topics = "product-updated")
    public void onProductUpdatedEvent(ConsumerRecord<String, ProductUpdatedEvent> consumerRecord) {
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