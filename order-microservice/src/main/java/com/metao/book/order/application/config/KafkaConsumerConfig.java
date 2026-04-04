package com.metao.book.order.application.config;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${kafka.consumer.concurrency:1}")
    private int consumerConcurrency;

    @Bean
    DeadLetterPublishingRecoverer orderDlqRecoverer(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    @Bean
    DefaultErrorHandler orderErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(500L, 3));
        handler.addNotRetryableExceptions(IllegalArgumentException.class, OrderNotFoundException.class);
        return handler;
    }

    @Bean
    public ConsumerFactory<String, OrderPaymentUpdatedEvent> orderPaymentEventConsumerFactory() {
        return createConsumerFactory(OrderPaymentUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentUpdatedEvent> orderPaymentEventKafkaListenerContainerFactory(
        DefaultErrorHandler orderErrorHandler,
        ConsumerFactory<String, OrderPaymentUpdatedEvent> orderPaymentEventConsumerFactory
    ) {
        var factory = createListenerContainerFactory(orderPaymentEventConsumerFactory, orderErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory() {
        return createConsumerFactory(OrderCreatedEvent.class);
    }

    @Bean
    public ConsumerFactory<String, OrderUpdatedEvent> orderUpdatedEventConsumerFactory() {
        return createConsumerFactory(OrderUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedEventKafkaListenerContainerFactory(
        DefaultErrorHandler orderErrorHandler,
        ConsumerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory
    ) {
        var factory = createListenerContainerFactory(orderCreatedEventConsumerFactory, orderErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> orderUpdatedEventKafkaListenerContainerFactory(
        DefaultErrorHandler orderErrorHandler,
        ConsumerFactory<String, OrderUpdatedEvent> orderUpdatedEventConsumerFactory
    ) {
        return createListenerContainerFactory(orderUpdatedEventConsumerFactory, orderErrorHandler);
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

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(
        ConsumerFactory<String, T> consumerFactory,
        DefaultErrorHandler orderErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(consumerConcurrency);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setCommonErrorHandler(orderErrorHandler);
        return factory;
    }

}
