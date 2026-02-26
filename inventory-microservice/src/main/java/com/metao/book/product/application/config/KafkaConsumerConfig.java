package com.metao.book.product.application.config;

import static com.metao.kafka.KafkaEventConfiguration.createConsumerFactory;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.kafka.KafkaClientProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    private final KafkaClientProperties kafkaProperties;
    @Value("${kafka.consumer.concurrency:1}")
    private int consumerConcurrency;

    @Bean
    DeadLetterPublishingRecoverer productDlqRecoverer(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    @Bean
    DefaultErrorHandler productErrorHandler(DeadLetterPublishingRecoverer productDlqRecoverer) {
        var handler = new DefaultErrorHandler(productDlqRecoverer, new FixedBackOff(500L, 3));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    @Bean
    public ConsumerFactory<String, ProductCreatedEvent> productCreatedEventConsumerFactory() {
        return createConsumerFactory(ProductCreatedEvent.class, kafkaProperties);
    }

    @Bean
    public ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory() {
        return createConsumerFactory(ProductUpdatedEvent.class, kafkaProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> productCreatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductCreatedEvent> productCreatedEventConsumerFactory,
        DefaultErrorHandler productErrorHandler
    ) {
        return createListenerContainerFactory(productCreatedEventConsumerFactory, productErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> productUpdatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory,
        DefaultErrorHandler productErrorHandler
    ) {
        return createListenerContainerFactory(productUpdatedEventConsumerFactory, productErrorHandler);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(
        ConsumerFactory<String, T> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(consumerConcurrency);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

}
