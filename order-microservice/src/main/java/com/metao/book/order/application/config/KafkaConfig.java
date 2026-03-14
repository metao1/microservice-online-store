package com.metao.book.order.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.order-created.name}") String kafkaTopic) {
        return createTopic(kafkaTopic);
    }

    @Bean
    public NewTopic orderPaymentTopic(@Value("${kafka.topic.order-payment.name}") String kafkaTopic) {
        return createTopic(kafkaTopic);
    }

    @Bean
    public NewTopic productUpdatedTopic(@Value("${kafka.topic.product-updated.name}") String kafkaTopic) {
        return createTopic(kafkaTopic);
    }

    private static NewTopic createTopic(String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(1)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .build();
    }
}
