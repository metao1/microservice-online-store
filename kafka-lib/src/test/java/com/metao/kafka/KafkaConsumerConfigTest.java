package com.metao.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;

@TestConfiguration
@EnableConfigurationProperties(KafkaClientProperties.class)
public class KafkaConsumerConfigTest {

    @Bean
    public ConsumerFactory<String, CreatedEventTest> createdEventConsumerFactory(
        KafkaClientProperties kafkaProperties
    ) {
        return KafkaEventConfiguration.createConsumerFactory(
            CreatedEventTest.class,
            kafkaProperties
        );
    }
}
