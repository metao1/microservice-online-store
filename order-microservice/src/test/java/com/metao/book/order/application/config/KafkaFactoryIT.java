package com.metao.book.order.application.config;

import static com.metao.book.order.OrderTestConstant.CUSTOMER_ID;
import static com.metao.book.order.OrderTestConstant.EUR;
import static com.metao.book.order.OrderTestConstant.PRICE;
import static com.metao.book.order.OrderTestConstant.PRODUCT_ID;
import static com.metao.book.order.OrderTestConstant.QUANTITY;
import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.order.OrderCreatedEvent;

import com.metao.book.shared.application.kafka.KafkaFactory;
import com.metao.shared.test.BaseKafkaIT;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaFactoryIT extends BaseKafkaIT {

    private final CountDownLatch latch = new CountDownLatch(5);

    @Autowired
    Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap;

    @RetryableTopic
    @KafkaListener(id = "order-listener-test", topics = "order-created")
    public void onEvent(ConsumerRecord<String, OrderCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }

    @Test
    @SneakyThrows
    @DisplayName("When sending Kafka multiple messages then all messages sent successfully")
    void testWhenSendingMultipleKafkaMessagesThenSentSuccessfully() {
        KafkaFactory<OrderCreatedEvent> kafkaFactory = (KafkaFactory<OrderCreatedEvent>) kafkaFactoryMap.get(
                OrderCreatedEvent.class);

        IntStream.range(0, 10).boxed().forEach(i -> kafkaFactory.submit("order-created", getCreatedEvent()));

        kafkaFactory.publish();
        latch.await(5, TimeUnit.SECONDS);

        assertThat(latch.getCount()).isZero();
    }

    private static OrderCreatedEvent getCreatedEvent() {
        return OrderCreatedEvent.newBuilder().setCustomerId(CUSTOMER_ID).setProductId(PRODUCT_ID)
                .setCurrency(EUR.toString()).setStatus(OrderCreatedEvent.Status.NEW).setPrice(PRICE.doubleValue())
                .setQuantity(QUANTITY.doubleValue()).build();
    }
}
