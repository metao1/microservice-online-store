package com.metao.book.product.infrastructure.factory.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.util.ProductEntityUtils;
import com.metao.shared.test.BaseKafkaIT;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaProductHandlerIT extends BaseKafkaIT {

    private static final String ASIN = "ASIN";
    private CountDownLatch latch;

    @Autowired
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.product-created.name}")
    private String productTopic;

    @MockitoBean
    private ProductService productService;

    @Test
    @SneakyThrows
    void handleGetProductEvent() {
        latch = new CountDownLatch(1);
        var event = ProductEntityUtils.productCreatedEvent();
        var pe = ProductEntityUtils.createProductEntity();

        when(productService.getProductByAsin(ASIN)).thenReturn(Optional.of(pe));

        kafkaTemplate.send(productTopic, event.getAsin(), event);

        latch.await(5, TimeUnit.SECONDS);

        assertThat(latch.getCount()).isZero();
    }

    @Test
    @SneakyThrows
    void whenSendingKafkaMessagesInParallelThenSuccessfullySentAll() {
        latch = new CountDownLatch(10);
        var event = ProductEntityUtils.productCreatedEvent();

        try (var ex = Executors.newFixedThreadPool(10)) {
            // send 10 parallel kafka publish tasks and wait for them to finish
            var futures = IntStream.rangeClosed(0, 10)
                    .mapToObj(i -> kafkaTemplate.send(productTopic, event.getAsin(), event)).toList();

            // wait for all futures to complete
            for (var future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);

        assertTrue(completed, "Not all messages were consumed in time");

        assertThat(latch.getCount()).isZero();
    }

    @RetryableTopic
    @KafkaListener(id = "${kafka.topic.product-created.id}-test", topics = "${kafka.topic.product-created.name}", groupId = "${kafka.topic.product-created.group-id}-test", containerFactory = "productCreatedEventKafkaListenerContainerFactory")
    public void onEvent(ConsumerRecord<String, ProductCreatedEvent> consumerRecord) {
        log.info("Consumed message -> {}", consumerRecord.offset());
        latch.countDown();
    }
}