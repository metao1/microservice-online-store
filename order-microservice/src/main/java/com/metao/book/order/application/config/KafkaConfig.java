package com.metao.book.order.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.order}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderStockTopic(@Value("${kafka.topic.order-stock}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderPaymentTopic(@Value("${kafka.topic.order-payment}") String topic) {
        return createTopic(topic);
    }

    private static NewTopic createTopic(String topicName) {
        return TopicBuilder
            .name(topicName)
            .partitions(1)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            //.compact()
            .build();
    }
}
