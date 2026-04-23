package com.metao.book.product.application.config;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.transaction.KafkaAwareTransactionManager;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private final KafkaProperties kafkaProperties;
    private final ObjectProvider<KafkaAwareTransactionManager<Object, Object>> kafkaTransactionManagerProvider;

    public KafkaConsumerConfig(
        KafkaProperties kafkaProperties,
        ObjectProvider<KafkaAwareTransactionManager<Object, Object>> kafkaTransactionManagerProvider
    ) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaTransactionManagerProvider = kafkaTransactionManagerProvider;
    }

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
        return createConsumerFactory(ProductCreatedEvent.class);
    }

    @Bean
    public ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory() {
        return createConsumerFactory(ProductUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> productCreatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductCreatedEvent> productCreatedEventConsumerFactory,
        DefaultErrorHandler productErrorHandler
    ) {
        var factory = createListenerContainerFactory(productCreatedEventConsumerFactory, productErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> productUpdatedEventKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductUpdatedEvent> productUpdatedEventConsumerFactory,
        DefaultErrorHandler productErrorHandler
    ) {
        var factory = createListenerContainerFactory(productUpdatedEventConsumerFactory, productErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(
        ConsumerFactory<String, T> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(consumerConcurrency);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.getContainerProperties().setKafkaAwareTransactionManager(kafkaTransactionManager);
        factory.getContainerProperties().setEosMode(ContainerProperties.EOSMode.V2);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> eventType) {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, eventType.getName());
        props.putAll(kafkaProperties.getProperties());
        return new DefaultKafkaConsumerFactory<>(props);
    }

}
