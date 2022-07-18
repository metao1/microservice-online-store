package com.metao.book.order;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class KafkaOrderConsumerConfiguration {

    @Bean
    KafkaOrderConsumer kafkaOrderConsumer() {
        return new KafkaOrderConsumer();
    }
}