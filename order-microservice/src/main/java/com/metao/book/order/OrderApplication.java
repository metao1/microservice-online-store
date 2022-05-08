package com.metao.book.order;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

@Slf4j
@EnableScheduling
@EnableKafkaStreams
@SpringBootApplication
public class OrderApplication {

    @Value("${spring.kafka.properties.schema.registry.url}")
    String srUrl;

    @Value("${spring.kafka.properties.basic.auth.credentials.source}")
    String crSource;

    @Value("${spring.kafka.properties.schema.registry.basic.auth.user.info}")
    String authUser;

    @Bean("order")
    public NewTopic orders(@Value("${kafka.stream.topic.order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean("order-payment")
    public NewTopic payment(@Value("${kafka.stream.topic.payment-order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean("stock-order")
    public NewTopic stock(@Value("${kafka.stream.topic.stock-order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    Map<String, String> serdeConfig() {
        return Map.of(SCHEMA_REGISTRY_URL_CONFIG, srUrl,
                BASIC_AUTH_CREDENTIALS_SOURCE, crSource,
                USER_INFO_CONFIG, authUser);
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
