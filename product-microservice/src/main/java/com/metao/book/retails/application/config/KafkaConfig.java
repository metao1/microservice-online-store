package com.metao.book.retails.application.config;

import static com.order.microservice.avro.Status.ACCEPT;
import static com.order.microservice.avro.Status.REJECT;

import java.util.Random;

import com.metao.book.retails.application.service.KafkaOrderProducer;
import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Reservation;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.validation.annotation.Validated;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class KafkaConfig {

    private final Random random = new Random();

    @Bean
    public KStream<String, OrderAvro> stream(@Value("${kafka.stream.topic.order}") String orderTopic,
            @Value("${kafka.stream.topic.stock-order}") String stockOrderTopic,
            KafkaOrderProducer template,
            StreamsBuilder builder) {
        var orderSerde = new SpecificAvroSerde<OrderAvro>();
        var rsvSerde = new SpecificAvroSerde<Reservation>();
        var stream = builder
                .stream(orderTopic, Consumed.with(Serdes.String(), orderSerde))
                .peek((k, order) -> log.info("New: {}", order));

        var customerOrderStoreSupplier = Stores.persistentKeyValueStore("customer-orders-new");

        Aggregator<String, OrderAvro, Reservation> aggregatorService = (id, order, rsv) -> {
            switch (order.getStatus()) {
                case CONFIRM -> rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
                case ROLLBACK -> {
                    // if (!order.getSource().equals("PAYMENT")) {
                    rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getPrice());
                    rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
                    // }
                }
                case NEW -> {
                    if (order.getPrice() <= rsv.getAmountAvailable()) {
                        rsv.setAmountAvailable(rsv.getAmountAvailable() - order.getPrice());
                        rsv.setAmountReserved(rsv.getAmountReserved() + order.getPrice());
                        order.setStatus(ACCEPT);
                    } else {
                        order.setStatus(REJECT);
                    }
                    template.send(stockOrderTopic, order.getOrderId(), order);
                }
                default -> {
                    
                }
            }
            log.info("{}", rsv);
            return rsv;
        };
        stream.selectKey((k, v) -> v.getCustomerId())
                .groupByKey(Grouped.with(Serdes.String(), orderSerde))
                .aggregate(
                        () -> Reservation.newBuilder().setAmountAvailable(random.nextDouble()).build(),
                        aggregatorService,
                        Materialized.<String, Reservation>as(customerOrderStoreSupplier)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(rsvSerde))
                .toStream()
                .peek((k, trx) -> log.info("Commit: {}", trx));

        return stream;
    }

}
