package com.metao.book.retails.application.config;

import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
public class KafkaConfig {

    @Value("${spring.kafka.properties.schema.registry.url}")
    String srUrl;

    @Value("${spring.kafka.properties.basic.auth.credentials.source}")
    String crSource;

    @Value("${spring.kafka.properties.schema.registry.basic.auth.user.info}")
    String authUser;

    @Bean
    SpecificAvroSerde<OrderAvro> orderAvroSerde() {
        SpecificAvroSerde<OrderAvro> serde = new SpecificAvroSerde<>();
        final Map<String, String>
                config =
                Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, srUrl,
                        "basic.auth.credentials.source", crSource,
                        AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, authUser);
        serde.configure(config, false);
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

}
