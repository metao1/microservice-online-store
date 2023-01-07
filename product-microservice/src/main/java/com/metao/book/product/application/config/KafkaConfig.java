package com.metao.book.product.application.config;

import static com.metao.book.shared.kafka.StreamsUtils.createTopic;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@RequiredArgsConstructor
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
    public NewTopic productTopic(@Value("${kafka.topic.product}") String topic) {
        return createTopic(topic);
    }

}