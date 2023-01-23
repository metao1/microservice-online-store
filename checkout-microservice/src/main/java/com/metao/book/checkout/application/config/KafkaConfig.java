package com.metao.book.checkout.application.config;

import com.metao.book.shared.kafka.RemoteKafkaService;
import com.metao.book.shared.kafka.StreamCustomExceptionHandlerConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import static com.metao.book.shared.kafka.StreamsUtils.createTopic;

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
@ComponentScan(basePackageClasses = {RemoteKafkaService.class, StreamCustomExceptionHandlerConfig.class})
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.order}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderPaymentTopic(@Value("${kafka.topic.order-payment}") String topic) {
        return createTopic(topic);
    }
}
