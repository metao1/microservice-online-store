package com.metao.book.cart.service.config;

import static com.metao.book.shared.kafka.StreamsUtils.getSpecificAvroSerdes;

import com.metao.book.shared.OrderEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerdesKafkaConfig {

    @Bean
    public SpecificAvroSerde<OrderEvent> orderValuesSerdes(KafkaProperties kafkaProperties) {
        return getSpecificAvroSerdes(kafkaProperties.getProperties());
    }
}
