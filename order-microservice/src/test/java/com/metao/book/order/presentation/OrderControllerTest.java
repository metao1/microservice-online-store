package com.metao.book.order.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.metao.book.order.KafkaOrderConsumer;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.CustomerId;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.shared.domain.financial.Currency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = KafkaOrderConsumerConfiguration.class)
@DirtiesContext
@EmbeddedKafka
@ActiveProfiles("test")
public class OrderControllerTest {

        private static Random RAND = new Random();

        @Autowired
        private KafkaOrderProducer kafkaProducer;

        @Autowired
        private KafkaOrderConsumer consumer;

        @Value("${kafka.stream.topic.order}")
        private String topic;

        @Test
        public void givenKakfaOrderTopic_whenSendingToTopic_thenMessageReceivedCurrectly() throws Exception {

                IntStream.range(0, 2)
                                .boxed()
                                .map(String::valueOf)
                                .map(s -> OrderDTO
                                                .builder()
                                                .orderId(new OrderId("order-" + s))
                                                .productId(new ProductId(s))
                                                .customerId(new CustomerId(s))
                                                .status(Status.NEW)
                                                .productCount(RAND.nextInt(10))
                                                .currency(Currency.EUR)
                                                .amount(1)
                                                .build())
                                .forEach(orderDTO -> kafkaProducer.send(topic, orderDTO.getOrderId().toUUID(),
                                                orderDTO));

                consumer.getLatch().await(2, TimeUnit.SECONDS);

                assertEquals(0, consumer.getLatch().getCount());
        }

}