package com.metao.book.order;

import com.metao.book.order.application.config.KafkaConfig;
import com.metao.book.order.application.config.SerdsConfig;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.order.kafka.SpringBootEmbeddedKafka;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext
@ContextConfiguration(classes = KafkaOrderConsumerConfiguration.class)
@ImportAutoConfiguration(classes = {KafkaProductConsumerConfiguration.class, SerdsConfig.class}, exclude = {KafkaConfig.class})
public class OrderControllerTest extends SpringBootEmbeddedKafka {

    private static final Random RAND = new Random();

    @Autowired
    private KafkaOrderProducer kafkaProducer;

    @Autowired
    private KafkaOrderConsumer consumer;

    @Autowired
    public ConsumerFactory<String, OrderAvro> consumerFactory;

    @Value("${kafka.stream.topic.order}")
    private String topic;

    @BeforeEach
    void setUp() {
        ContainerProperties containerProperties = new ContainerProperties("orders");
        var container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.start();
        ContainerTestUtils.waitForAssignment(container, kafkaEmbeddedBroker.getPartitionsPerTopic());
    }

    @Test
    public void givenKafkaOrderTopic_whenSendingToTopic_thenMessageReceivedCorrectly() throws Exception {

        IntStream.range(0, 1)
                .boxed()
                .map(String::valueOf)
                .map(s -> OrderAvro
                        .newBuilder()
                        .setOrderId(Long.parseLong(s))
                        .setProductId("product - " + s)
                        .setCustomerId(Long.parseLong(s))
                        .setStatus(Status.NEW)
                        .setQuantity(1)
                        .setPrice(RAND.nextInt(100))
                        .setCurrency(Currency.dlr)
                        .build())
                .forEach(orderDTO -> kafkaProducer.send(topic, orderDTO.getOrderId(),
                        orderDTO));

        consumer.getLatch().await(6, TimeUnit.SECONDS);

        assertEquals(0, consumer.getLatch().getCount());
    }

}