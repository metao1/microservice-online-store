package com.metao.book.order.kafka;

import com.metao.book.shared.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.validation.annotation.Validated;

@Validated
@TestConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaProductConsumerConfiguration {

    private final KafkaProperties properties;

    @Bean
    public KafkaTemplate<String, OrderEvent> defaultKafkaTemplate(
        ObjectProvider<RecordMessageConverter> messageConverter,
        ProducerFactory<String, OrderEvent> defaultKafkaProducerFactory
    ) {
        var kafkaTemplate = new KafkaTemplate<>(defaultKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        return kafkaTemplate;
    }

    @Bean
    public ProducerFactory<String, OrderEvent> defaultKafkaProducerFactory() {
        var configProps = this.properties.buildProducerProperties();
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, "0");
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, "0");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

}