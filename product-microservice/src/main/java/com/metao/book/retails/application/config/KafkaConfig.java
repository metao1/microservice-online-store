package com.metao.book.retails.application.config;

import com.metao.book.retails.application.service.KafkaOrderProducer;
import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Reservation;
import com.order.microservice.avro.Status;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.validation.annotation.Validated;

import java.util.Random;

import static com.order.microservice.avro.Status.ACCEPT;
import static com.order.microservice.avro.Status.REJECT;

@Slf4j
@Validated
@Configuration
//@EnableKafkaStreams
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    private final Random random = new Random();
//
//    @Bean("order-payment")
//    public KStream<String, OrderAvro> stream(@Value("${kafka.stream.topic.payment-order}") String paymentOrderTopic,
//                                             @Value("${kafka.stream.topic.order}") String orderTopic,
//                                             KafkaOrderProducer template,
//                                             StreamsBuilder builder) {
//        var orderSerde = new SpecificAvroSerde<OrderAvro>();
//        var rsvSerde = new SpecificAvroSerde<Reservation>();
//        KStream<String, OrderAvro> stream = builder
//                .stream(orderTopic, Consumed.with(Serdes.String(), orderSerde))
//                .peek((k, order) -> log.info("New: {}", order));
//
//        KeyValueBytesStoreSupplier customerOrderStoreSupplier =
//                Stores.persistentKeyValueStore("stock-order");
//        Aggregator<String, OrderAvro, Reservation> aggregatorService = (id, order, rsv) -> {
//
//            if (order.getStatus().equals(Status.CONFIRM)) {
//                rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
//            } else if (order.getStatus().equals(Status.ROLLBACK)) {
//                //if (!order.getSource().equals("PAYMENT")) {
//                rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getPrice());
//                rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
//                //}
//            } else if (order.getStatus().equals(Status.NEW)) {
//                if (order.getPrice() <= rsv.getAmountAvailable()) {
//                    rsv.setAmountAvailable(rsv.getAmountAvailable() - order.getPrice());
//                    rsv.setAmountReserved(rsv.getAmountReserved() + order.getPrice());
//                    order.setStatus(ACCEPT);
//                } else {
//                    order.setStatus(REJECT);
//                }
//                template.send(paymentOrderTopic, order.getOrderId(), order);
//            }
//
//            log.info("{}", rsv);
//            return rsv;
//        };
//
//        stream.selectKey((k, v) -> v.getCustomerId())
//                .groupByKey(Grouped.with(Serdes.String(), orderSerde))
//                .aggregate(
//                        () -> Reservation.newBuilder().setAmountAvailable(random.nextDouble()).build(),
//                        aggregatorService,
//                        Materialized.<String, Reservation>as(customerOrderStoreSupplier)
//                                .withKeySerde(Serdes.String())
//                                .withValueSerde(rsvSerde))
//                .toStream()
//                .peek((k, trx) -> log.info("Commit: {}", trx));
//
//        return stream;
//    }
}
