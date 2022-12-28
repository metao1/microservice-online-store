package com.metao.book.checkout.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import com.metao.book.checkout.domain.OrderJoiner;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.ReservationEvent;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
@ImportAutoConfiguration(value = KafkaConfig.class)
public class ReservationStreamConfig {

        private final OrderJoiner orderJoiner;

        @Bean
        public KStream<String, OrderEvent> orderStream(
                        StreamsBuilder builder,
                        NewTopic orderProductTopic,
                        NewTopic reservationTopic,
                        NewTopic orderTopic,
                        SpecificAvroSerde<OrderEvent> orderSerdes,
                        SpecificAvroSerde<ReservationEvent> reservationSerdes) {

                var reservationStream = builder.stream(reservationTopic.name(),
                                Consumed.with(Serdes.String(), reservationSerdes));
                var orderStream = builder.stream(orderTopic.name(), Consumed.with(Serdes.String(), orderSerdes));
                KeyValueBytesStoreSupplier store = Stores.persistentKeyValueStore("reservation");
                orderStream
                                .selectKey((k, v) -> v.getProductId())
                                .join(reservationStream.toTable(Materialized.<String, ReservationEvent>as(store)
                                                .withKeySerde(Serdes.String())
                                                .withValueSerde(reservationSerdes)), orderJoiner)
                                .peek((k, order) -> log.info("new-order:{}", order));

                orderStream.to(orderProductTopic.name(), Produced.with(Serdes.String(), orderSerdes));
                return orderStream;
        }
}
