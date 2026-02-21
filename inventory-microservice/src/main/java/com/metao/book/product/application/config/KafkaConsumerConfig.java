package com.metao.book.product.application.config;

import static com.metao.kafka.KafkaEventConfiguration.createConsumerFactory;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.kafka.KafkaClientProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
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
    public ConsumerFactory<String, ProductCreatedEvent> productPaymentEventConsumerFactory() {
        return createConsumerFactory(ProductCreatedEvent.class, kafkaProperties);
    }

    @Bean
    public ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory() {
        return createConsumerFactory(ProductUpdatedEvent.class, kafkaProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> productCreatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductCreatedEvent> productPaymentEventConsumerFactory,
        DefaultErrorHandler productErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productPaymentEventConsumerFactory);
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(productErrorHandler);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> productUpdatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory,
        DefaultErrorHandler productErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productUpdatedEventConsumerFactory);
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(productErrorHandler);

        return factory;
    }

}
