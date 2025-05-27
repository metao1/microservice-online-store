package com.metao.book.payment;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderPaymentEvent;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer; // For test producer config
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig; // For test producer config
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer; // For test producer config
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration; // For test-specific beans
import org.springframework.context.annotation.Bean; // For test-specific beans
import org.springframework.kafka.core.DefaultKafkaProducerFactory; // For test producer config
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory; // For test producer config
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.HashMap; // For test producer config
import java.util.Map; // For test producer config

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = { "${kafka.topic.order-created.name}", "${kafka.topic.order-payment.name}" },
    brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" }
)
@DirtiesContext
@ActiveProfiles("test")
class PaymentKafkaIntegrationTest {

    @TestConfiguration
    static class KafkaTestProducerConfiguration {

        @Autowired
        private EmbeddedKafkaBroker embeddedKafkaBroker;

        @Bean
        public ProducerFactory<String, OrderCreatedEvent> orderCreatedEventProducerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
            // props.put("schema.registry.url", "mock://test-schema-registry"); // Optional: if schema reg is strictly needed by serializer for test
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate(
                ProducerFactory<String, OrderCreatedEvent> orderCreatedEventProducerFactory) {
            return new KafkaTemplate<>(orderCreatedEventProducerFactory);
        }
    }


    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${kafka.topic.order-created.name}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.order-payment.name}")
    private String orderPaymentTopic;

    @Test
    void testOrderPaymentFlow_consumesOrderCreated_producesOrderPaymentEvent() {
        String orderId = "integrationTestOrderId-" + System.currentTimeMillis();
        OrderCreatedEvent orderCreated = OrderCreatedEvent.newBuilder()
                .setId(orderId)
                .setCustomerId("custIntegrationTest")
                .setProductId("prodIntegrationTest")
                .setPrice(19.99)
                .setQuantity(1.0)
                .setCurrency("USD")
                .setStatus(OrderCreatedEvent.Status.NEW)
                .build();

        orderCreatedEventKafkaTemplate.send(orderCreatedTopic, orderId, orderCreated);

        // KafkaTestUtils.getSingleRecord will use consumer properties from the application context
        // for the value deserializer (OrderPaymentEvent).
        // The group ID is specific to this test consumer.
        ConsumerRecord<String, OrderPaymentEvent> receivedRecord = KafkaTestUtils.getSingleRecord(
                embeddedKafkaBroker.getBrokersAsString(), // Broker URL
                "test-payment-integration-consumer-group", // Consumer group for this test
                orderPaymentTopic, // Topic to consume from
                Duration.ofSeconds(20) // Timeout for receiving the message
        );
        
        assertThat(receivedRecord).isNotNull();
        OrderPaymentEvent paymentEvent = receivedRecord.value();
        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getOrderId()).isEqualTo(orderId);
        assertThat(paymentEvent.getStatus()).isIn(OrderPaymentEvent.Status.SUCCESSFUL, OrderPaymentEvent.Status.FAILED);
        assertThat(paymentEvent.getPaymentId()).isNotNull().isNotEmpty();
    }
}
```
