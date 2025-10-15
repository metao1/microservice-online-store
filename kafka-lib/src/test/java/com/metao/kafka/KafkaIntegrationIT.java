package com.metao.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.metao.shared.test.KafkaContainer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {
    KafkaEventHandler.class, KafkaEventConfiguration.class
})
class KafkaIntegrationIT extends KafkaContainer {

    private final CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    KafkaEventHandler eventHandler;

    @Test
    @SneakyThrows
    void testOrderPaymentFlow_consumesOrderCreated_producesOrderPaymentEvent() {
        String orderId = "integrationTestOrderId-" + System.currentTimeMillis();
        CreatedEventTest orderCreated = CreatedEventTest.newBuilder()
            .setId(orderId)
            .setCustomerId("custIntegrationTest")
            .setProductId("prodIntegrationTest")
            .setPrice(19.99)
            .setQuantity(1.0)
            .setCurrency("USD")
            .setStatus(CreatedEventTest.Status.NEW)
            .build();

        eventHandler.send(orderId, orderCreated);
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertThat(latch.getCount()).isZero());
        assertThat(latch.getCount()).isZero();
    }

    @KafkaListener(id = "${kafka.topic.created-event-test.id}", topics = "${kafka.topic.created-event-test.name}")
    public void onOrderUpdatedEvent(ConsumerRecord<String, CreatedEventTest> consumerRecord) {
        latch.countDown();
    }
}
