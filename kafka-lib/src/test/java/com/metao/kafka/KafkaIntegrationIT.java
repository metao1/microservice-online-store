package com.metao.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.shared.test.BaseKafkaTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
    classes = {KafkaEventConfiguration.class, KafkaEventHandler.class}
)
public class KafkaIntegrationIT extends BaseKafkaTest {

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

        eventHandler.handle(orderId, orderCreated);
        latch.await(10, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isZero();
    }

    @RetryableTopic
    @KafkaListener(id = "create-event-test-id", topics = "${kafka.topic.created-event-test.name}")
    public void onOrderUpdatedEvent(ConsumerRecord<String, CreatedEventTest> consumerRecord) {
        latch.countDown();
    }
}
