package com.metao.book.product.application.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Bean
    public NewTopic productCreatedTopic(@Value("${kafka.topic.product-created.name}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic productUpdatedTopic(@Value("${kafka.topic.product-updated.name}") String topic) {
        return createTopic(topic);
    }

    private static NewTopic createTopic(String topicName) {
        return TopicBuilder
            .name(topicName)
            .partitions(1)
            .build();
    }
}