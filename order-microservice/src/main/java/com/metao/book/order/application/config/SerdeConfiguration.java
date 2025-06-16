package com.metao.book.order.application.config;

import com.metao.book.shared.OrderPaymentEvent;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
public class SerdeConfiguration {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaProtobufSerde<OrderPaymentEvent> accountSerde() {
        var serde = new KafkaProtobufSerde<>(OrderPaymentEvent.class);

        serde.configure(kafkaProperties.getProperties(), false);
        log.debug("Created serde for {}", OrderPaymentEvent.class);
        return serde;
    }
}