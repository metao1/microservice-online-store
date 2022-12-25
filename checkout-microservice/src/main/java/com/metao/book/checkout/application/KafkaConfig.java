package com.metao.book.checkout.application;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@EnableKafka
@RequiredArgsConstructor
@EnableConfigurationProperties({ KafkaProperties.class })
public class KafkaConfig {

    @Bean
    public NewTopic paymentTopic(@Value("${kafka.topic.payment}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(1)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }
}
