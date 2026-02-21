package com.metao.book.order.application.config;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.kafka.KafkaClientProperties;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
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
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    DeadLetterPublishingRecoverer orderDlqRecoverer(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    @Bean
    DefaultErrorHandler orderErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(500L, 3));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    @Bean
    public ConsumerFactory<String, OrderPaymentEvent> orderPaymentEventConsumerFactory() {
        var ps = kafkaProperties.getProperties();
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, OrderPaymentEvent.class.getName());

        props.putAll(ps);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentEvent> orderPaymentEventKafkaListenerContainerFactory(
        DefaultErrorHandler orderErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderPaymentEvent>();
        factory.setConsumerFactory(orderPaymentEventConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.setCommonErrorHandler(orderErrorHandler);
        return factory;
    }

    // Configuration for OrderCreatedEvent
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory() {
        var ps = kafkaProperties.getProperties();
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, OrderCreatedEvent.class.getName());

        props.putAll(ps);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedEventKafkaListenerContainerFactory(
        DefaultErrorHandler orderErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>();
        factory.setConsumerFactory(orderCreatedEventConsumerFactory());

        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.setCommonErrorHandler(orderErrorHandler);

        return factory;
    }

    // Configuration for OrderCreatedEvent
    @Bean
    public ConsumerFactory<String, OrderUpdatedEvent> orderUpdatedEventConsumerFactory() {
        var ps = kafkaProperties.getProperties();
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, OrderUpdatedEvent.class.getName());

        props.putAll(ps);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> orderUpdatedEventKafkaListenerContainerFactory(
        DefaultErrorHandler orderErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent>();
        factory.setConsumerFactory(orderUpdatedEventConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.setCommonErrorHandler(orderErrorHandler);
        return factory;
    }

}
