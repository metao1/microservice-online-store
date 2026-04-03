package com.metao.book.order.application.config;

import static com.metao.book.order.OrderTestConstant.CUSTOMER_ID;
import static com.metao.book.order.OrderTestConstant.EUR;
import static com.metao.book.order.OrderTestConstant.PRICE;
import static com.metao.book.order.OrderTestConstant.PRODUCT_ID;
import static com.metao.book.order.OrderTestConstant.QUANTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.shared.test.KafkaContainer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class KafkaFactoryIT extends KafkaContainer {

    private final CountDownLatch latch = new CountDownLatch(10);

    @Autowired
    KafkaTemplate<String, OrderCreatedEvent> kafkaEventHandler;

    @Autowired
    KafkaListenerEndpointRegistry registry;

    @BeforeEach
    void waitForKafkaAssignment() {
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("When sending Kafka multiple messages then all messages sent successfully")
    void testWhenSendingMultipleKafkaMessagesThenSentSuccessfully() {

        IntStream.range(0, 10).boxed()
            .forEach(i -> kafkaEventHandler.send("order-created", String.valueOf(i), buildOrderCreatedEvent()));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
                assertThat(latch.getCount()).describedAs("Messages remaining after wait")
                    .isLessThanOrEqualTo(5);
            }
        );
    }

    @RetryableTopic
    @KafkaListener(id = "order-listener-test",
        topics = "order-created",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory")
    public void onEvent(ConsumerRecord<String, OrderCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }

    private static OrderCreatedEvent buildOrderCreatedEvent() {
        return OrderCreatedEvent.newBuilder()
            .setUserId(CUSTOMER_ID)
            .setStatus(OrderCreatedEvent.Status.CREATED)
            .addItems(OrderCreatedEvent.OrderItem.newBuilder()
                .setSku(PRODUCT_ID)
                .setProductTitle("Kafka Factory Product")
                .setQuantity(QUANTITY.doubleValue())
                .setPrice(PRICE.doubleValue())
                .setCurrency(EUR.toString())
                .build())
            .build();
    }
}
