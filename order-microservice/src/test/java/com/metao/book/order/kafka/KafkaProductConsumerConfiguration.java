package com.metao.book.order.kafka;

import com.order.microservice.avro.OrderAvro;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.validation.annotation.Validated;

@Validated
@TestConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaProductConsumerConfiguration {

    private final KafkaProperties properties;

    /**
     * @see KafkaAutoConfiguration#kafkaProducerFactory(ObjectProvider) ()
     */
    @Bean
    public ProducerFactory<Long, OrderAvro> defaultKafkaProducerFactory() {
        var configProps = this.properties.buildProducerProperties();
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, "0");
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, "0");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        return new DefaultKafkaProducerFactory<>(this.properties.buildProducerProperties());
    }

    /**
     * @see KafkaAutoConfiguration#kafkaTemplate
     * (
     * org.springframework.kafka.core.ProducerFactory,
     * org.springframework.kafka.support.ProducerListener,
     * org.springframework.beans.factory.ObjectProvider
     * )
     */
    @Bean
    public KafkaTemplate<Long, OrderAvro> defaultKafkaTemplate(
            ObjectProvider<RecordMessageConverter> messageConverter,
            ProducerFactory<Long, OrderAvro> defaultKafkaProducerFactory
    ) {
        var kafkaTemplate = new KafkaTemplate<>(defaultKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setDefaultTopic(this.properties.getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderAvro> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderAvro> consumerFactory() {
        var configProps = this.properties.buildProducerProperties();
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new JsonDeserializer<>(OrderAvro.class));
    }

}