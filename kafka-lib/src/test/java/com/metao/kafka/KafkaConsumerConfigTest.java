package com.metao.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@TestConfiguration
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConsumerConfigTest {

    @Bean
    NewTopic createdEventTestTopic() {
        return TopicBuilder.name("created-event-test")
            .partitions(1)
            .replicas(1)
            .build();
    }
}
