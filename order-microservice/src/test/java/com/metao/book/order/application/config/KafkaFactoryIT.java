package com.metao.book.order.application.config;

import static com.metao.book.order.OrderTestConstant.CUSTOMER_ID;
import static com.metao.book.order.OrderTestConstant.EUR;
import static com.metao.book.order.OrderTestConstant.PRICE;
import static com.metao.book.order.OrderTestConstant.PRODUCT_ID;
import static com.metao.book.order.OrderTestConstant.QUANTITY;
import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderCreatedEvent;
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
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

@Slf4j
@Import({KafkaConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaFactoryIT extends BaseKafkaIT {

    private final CountDownLatch latch = new CountDownLatch(5);

    @Autowired
    KafkaFactory<OrderCreatedEvent> kafkaFactory;

    @RetryableTopic
    @KafkaListener(id = "order-listener-test", topics = "topic-test")
    public void onEvent(ConsumerRecord<String, String> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }

    @Test
    @SneakyThrows
    @DisplayName("When sending Kafka multiple messages then all messages sent successfully")
    void testWhenSendingMultipleKafkaMessagesThenKafkaTemplateSent() {
        kafkaFactory.subscribe();
        IntStream.range(0, 10).boxed().forEach(i -> kafkaFactory.submit("topic-test", "key-" + i, getCreatedEvent()));

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
