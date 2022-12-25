package com.metao.book.product.application.config;

import com.metao.book.shared.OrderEvent;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.validation.annotation.Validated;

import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import com.metao.book.shared.ReservationEvent;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.order}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic productTopic(@Value("${kafka.topic.product}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic reservationTopic(@Value("${kafka.topic.reservation}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderProductTopic(@Value("${kafka.topic.order-product}") String topic) {
        return createTopic(topic);
    }

    @Bean
    SpecificAvroSerde<ProductsResponseEvent> productResponseEventSpecificAvroSerde(KafkaProperties kafkaProperties) {
        return createAvroSerde(kafkaProperties);
    }

    @Bean
    public SpecificAvroSerde<OrderEvent> orderValuesSerdes(KafkaProperties kafkaProperties) {
        return createAvroSerde(kafkaProperties);
    }

    @Bean
    public SpecificAvroSerde<ProductEvent> productValuesSerdes(KafkaProperties kafkaProperties) {
        return createAvroSerde(kafkaProperties);
    }

    @Bean
    SpecificAvroSerde<ReservationEvent> reservationValuesSerdes(KafkaProperties kafkaProperties) {
        return createAvroSerde(kafkaProperties);
    }

    private <T extends SpecificRecord> SpecificAvroSerde<T> createAvroSerde(KafkaProperties kafkaProperties) {
        var result = new SpecificAvroSerde<T>();
        result.configure(kafkaProperties.getProperties(), false);
        return result;
    }

    private NewTopic createTopic(String topicName) {
        return TopicBuilder
                .name(topicName)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, false);
    }

}
