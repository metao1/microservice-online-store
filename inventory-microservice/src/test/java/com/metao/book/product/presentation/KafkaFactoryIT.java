package com.metao.book.product.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.product.event.ProductCreatedEvent;
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

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaFactoryIT extends BaseKafkaIT {

    private final CountDownLatch latch = new CountDownLatch(10);

    @Autowired
    Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap;

    @RetryableTopic
    @KafkaListener(id = "product-created-listener-test",
        topics = "product-created",
        containerFactory = "productCreatedEventKafkaListenerContainerFactory")
    public void onEvent(ConsumerRecord<String, ProductCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }

    @Test
    @SneakyThrows
    @DisplayName("When sending Kafka multiple messages then all messages sent successfully")
    void testWhenSendingMultipleKafkaMessagesThenSentSuccessfully() {
        KafkaFactory<ProductCreatedEvent> kafkaFactory = (KafkaFactory<ProductCreatedEvent>)
            kafkaFactoryMap.get(ProductCreatedEvent.class);
        kafkaFactory.subscribe();
        IntStream.range(0, 10).boxed().forEach(i -> kafkaFactory.submit(i + "", getCreatedEvent()));

        kafkaFactory.publish();
        latch.await(5, TimeUnit.SECONDS);

        assertThat(latch.getCount()).isZero();
    }

    private static ProductCreatedEvent getCreatedEvent() {
        return ProductCreatedEvent.newBuilder().build();
    }
}
