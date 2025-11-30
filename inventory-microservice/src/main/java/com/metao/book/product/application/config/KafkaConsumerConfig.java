package com.metao.book.product.application.config;

import static com.metao.kafka.KafkaEventConfiguration.createConsumerFactory;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
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

    private final KafkaProperties kafkaProperties;
    @Value("${spring.kafka.bootstrap-servers}")

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
        ConsumerFactory<String, ProductCreatedEvent> productPaymentEventConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productPaymentEventConsumerFactory);
        factory.setConcurrency(1);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> productUpdatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productUpdatedEventConsumerFactory);
        factory.setConcurrency(1);

        return factory;
    }

}
