package com.metao.book.product.application.config;

import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.validation.annotation.Validated;

@Validated
@EnableKafka
@Configuration
public class KafkaConfig {

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
    SpecificAvroSerde<OrderAvro> orderAvroSerde(KafkaProperties properties) {
        SpecificAvroSerde<OrderAvro> serde = new SpecificAvroSerde<>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    SpecificAvroSerde<ProductEvent> productEventSpecificAvroSerde(KafkaProperties kafkaProperties) {
        var result = new SpecificAvroSerde<ProductEvent>();
        result.configure(kafkaProperties.getProperties(), false);
        return result;
    }

    @Bean
    SpecificAvroSerde<ProductsResponseEvent> productResponseEventSpecificAvroSerde(KafkaProperties kafkaProperties) {
        var result = new SpecificAvroSerde<ProductsResponseEvent>();
        result.configure(kafkaProperties.getProperties(), false);
        return result;
    }

    @Bean
    CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, false);
    }

}
