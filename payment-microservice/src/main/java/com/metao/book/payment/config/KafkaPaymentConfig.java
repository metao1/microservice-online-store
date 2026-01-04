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

    @Value("${kafka.topic.order-payment.name}")
    private String orderPaymentTopicName;

    @Bean
    public NewTopic orderPaymentTopic() {
        return TopicBuilder.name(orderPaymentTopicName)
            .partitions(1)
            .replicas(1)
            .build();
    }
}
