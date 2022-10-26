package com.metao.book.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.metao.book.order.application.config.KafkaConfig;
import com.metao.book.order.application.config.SerdsConfig;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.order.kafka.SpringBootEmbeddedKafka;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.Status;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ImportAutoConfiguration(classes = {KafkaOrderConsumer.class, KafkaProductConsumerConfiguration.class,
    SerdsConfig.class}, exclude = {KafkaConfig.class})
public class OrderControllerTest extends SpringBootEmbeddedKafka {

    private static final Random RAND = new Random();

    @Autowired
    private KafkaOrderProducer kafkaProducer;

    @Autowired
    private KafkaOrderConsumer consumer;

    @Value("${kafka.topic.order}")
    private String topic;

    @Test
    public void givenKafkaOrderTopic_whenSendingToTopic_thenMessageReceivedCorrectly() throws Exception {
        IntStream.range(0, 1)
            .boxed()
            .map(String::valueOf)
            .map(this::createOrderFromCustomerId)
            .forEach(orderDTO -> kafkaProducer.send(topic, orderDTO.getOrderId(), orderDTO));

        consumer.getLatch().await(6, TimeUnit.SECONDS);

        assertEquals(0, consumer.getLatch().getCount());
    }

    private OrderAvro createOrderFromCustomerId(String customerId) {
        return OrderAvro.newBuilder()
            .setOrderId(customerId)
            .setProductId("product - " + customerId)
            .setCustomerId(customerId)
            .setSource("PAYMENT")
            .setStatus(Status.NEW)
            .setQuantity(1)
            .setPrice(RAND.nextInt(100))
            .setCurrency(Currency.dlr)
            .build();
    }

}