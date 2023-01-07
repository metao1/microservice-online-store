package com.metao.book.order.application.config;

import static com.metao.book.shared.kafka.StreamsUtils.getSpecificAvroSerdes;

import com.metao.book.shared.OrderEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaSerdesConfig {

    @Bean
    SpecificAvroSerde<OrderEvent> orderEventSerde(KafkaProperties kafkaProperties) {
        return getSpecificAvroSerdes(kafkaProperties.getProperties());
    }
}
