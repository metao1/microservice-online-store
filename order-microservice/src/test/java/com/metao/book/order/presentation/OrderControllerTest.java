package com.metao.book.order.presentation;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka
public class OrderControllerTest {

        @Autowired
        private KafkaConsumer consumer;

        @Autowired
        private KafkaProducer producer;

        @Value("${test.topic}")
        private String topic;

        @Test
        public void givenEmbeddedKafkaBroker_whenSendingtoSimpleProducer_thenMessageReceived()
                        throws Exception {
                producer.send(topic, null);
                consumer.paused();
                assertThat(consumer.getLatch().getCount(), equalTo(0L));
                assertThat(consumer.getPayload(), containsString("embedded-test-topic"));
        }
}
