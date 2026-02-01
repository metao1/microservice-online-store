package com.metao.book.payment.config;

import com.metao.book.shared.domain.base.DelegatingDomainEventTranslator;
import com.metao.kafka.KafkaEventHandler;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Import({KafkaEventHandler.class, DelegatingDomainEventTranslator.class})
public class KafkaPaymentConfig {

    @Bean
    public NewTopic orderPaymentTopic(@Value("${kafka.topic.order-payment.name}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderCreatedTopic(@Value("${kafka.topic.order-created.name}") String topic) {
        return createTopic(topic);
    }

    private static NewTopic createTopic(String topicName) {
        return TopicBuilder
            .name(topicName)
            .partitions(1)
            .build();
    }
}
