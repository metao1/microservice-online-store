package com.metao.book.order.application.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orders(@Value("${kafka.topic.order}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(3)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

    @Bean
    public NewTopic payment(@Value("${kafka.topic.payment}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(3)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

    @Bean
    public NewTopic stock(@Value("${kafka.topic.stock}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(3)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

}
