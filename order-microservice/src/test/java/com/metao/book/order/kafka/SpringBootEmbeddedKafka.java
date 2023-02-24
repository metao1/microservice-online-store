package com.metao.book.order.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@EmbeddedKafka(count = 3, brokerProperties = "listeners:PLAINTEXT://:9093,PLAINTEXT://:9094,PLAINTEXT://:9095")
public class SpringBootEmbeddedKafka {
    @Test
    public void test(EmbeddedKafkaBroker broker) {
        String brokerList = broker.getBrokersAsString();
        System.out.println(brokerList);
    }
}