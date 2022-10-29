package com.metao.book.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.metao.book.order.application.config.KafkaConfig;
import com.metao.book.order.application.config.KafkaStreamConfig;
import com.metao.book.order.application.config.SerdsConfig;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.order.kafka.SpringBootEmbeddedKafka;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.Status;
import com.metao.book.shared.test.TestUtils.StreamBuilder;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "test")
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(classes = {KafkaProductConsumerConfiguration.class, KafkaOrderConsumerTestConfig.class,
    SerdsConfig.class}, exclude = {KafkaConfig.class, KafkaStreamConfig.class})
public class OrderControllerTest extends SpringBootEmbeddedKafka {

    private static final Random RAND = new Random();

    @Autowired
    private KafkaOrderProducer kafkaProducer;

    @Autowired
    private KafkaOrderConsumerTestConfig consumer;

    private final String topic = "order";

    @Test
    public void givenKafkaOrderTopic_whenSendingToTopic_thenMessageReceivedCorrectly() throws Exception {
        StreamBuilder.of(OrderAvro.class, 0, 10, this::createOrderFromCustomerId)
            .forEach(orderDTO -> kafkaProducer.send(topic, orderDTO.getOrderId(), orderDTO));

        consumer.getLatch().await(6, TimeUnit.SECONDS);

        assertEquals(0, consumer.getLatch().getCount());
    }

    private OrderAvro createOrderFromCustomerId(int randomId) {
        return OrderAvro.newBuilder()
            .setOrderId("order-" + randomId)
            .setProductId("product - " + randomId)
            .setCustomerId("customer_id")
            .setSource("PAYMENT")
            .setStatus(Status.NEW)
            .setQuantity(1)
            .setPrice(RAND.nextInt(100))
            .setCurrency(Currency.dlr)
            .build();
    }

}