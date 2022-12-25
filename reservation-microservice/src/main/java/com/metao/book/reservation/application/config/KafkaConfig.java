package com.metao.book.reservation.application.config;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import com.metao.book.shared.ReservationEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
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

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    public NewTopic productTopic(@Value("${kafka.topic.product}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    public NewTopic reservationTopic(@Value("${kafka.topic.reservation}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    public NewTopic orderProductTopic(@Value("${kafka.topic.order-product}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    SpecificAvroSerde<ProductsResponseEvent> productResponseEventSpecificAvroSerde(KafkaProperties kafkaProperties) {
        var result = new SpecificAvroSerde<ProductsResponseEvent>();
        result.configure(kafkaProperties.getProperties(), false);
        return result;
    }

    @Bean
    public SpecificAvroSerde<OrderEvent> orderValuesSerdes(KafkaProperties properties) {
        var serde = new SpecificAvroSerde<OrderEvent>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    public SpecificAvroSerde<ProductEvent> productValuesSerdes(KafkaProperties properties) {
        var serde = new SpecificAvroSerde<ProductEvent>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    SpecificAvroSerde<ReservationEvent> reservationValuesSerdes(KafkaProperties properties) {
        var serde = new SpecificAvroSerde<ReservationEvent>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, false);
    }

}
