package com.metao.book.payment.application.config;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    @Value("${kafka.consumer.concurrency:1}")
    private int consumerConcurrency;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentEvent> orderPaymentEventConcurrentKafkaListenerContainerFactory(
        ConsumerFactory<String, OrderPaymentEvent> orderPaymentEventConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderPaymentEvent>();
        factory.setConsumerFactory(orderPaymentEventConsumerFactory);
        factory.setConcurrency(consumerConcurrency);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>();
        factory.setConsumerFactory(orderCreatedEventConsumerFactory);
        factory.setConcurrency(consumerConcurrency);
        return factory;
    }

}

