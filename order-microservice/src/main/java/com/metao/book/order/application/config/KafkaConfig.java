package com.metao.book.order.application.config;

import com.metao.book.order.domain.OrderManageService;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Reservation;
import com.order.microservice.avro.Status;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executor;

import static com.order.microservice.avro.Status.ACCEPT;
import static com.order.microservice.avro.Status.REJECT;
import static org.apache.kafka.streams.kstream.Grouped.with;

@Slf4j
@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    private final Random random = new Random();
    private static final long TIMEOUT = 10;
    private final OrderManageService orderManageService;
    private final KafkaProperties properties;

    @Bean
    SpecificAvroSerde<OrderAvro> orderAvroSerde() {
        var serde = new SpecificAvroSerde<OrderAvro>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    public KTable<String, OrderAvro> table(@Value("${kafka.stream.topic.order}") String orderTopic, SpecificAvroSerde<OrderAvro> specificAvroSerde, StreamsBuilder sb) {
        var store = Stores.persistentKeyValueStore(orderTopic);
        var stream = sb.stream(orderTopic, Consumed.with(Serdes.String(), specificAvroSerde));
        return stream.toTable(Materialized.<String, OrderAvro>as(store)
                .withKeySerde(Serdes.String())
                .withValueSerde(specificAvroSerde));
    }

    @Bean
    public KStream<String, OrderAvro> stream(@Value("${kafka.stream.topic.order}") String orderTopic,
                                             @Value("${kafka.stream.topic.payment-order}") String paymentOrderTopic,
                                             @Value("${kafka.stream.topic.stock-order}") String stockOrderTopic,
                                             StreamsBuilder builder) {
        var rsvSerde = new SpecificAvroSerde<OrderAvro>();
        KStream<String, OrderAvro> paymentOrders = builder
                .stream(paymentOrderTopic, Consumed.with(Serdes.String(), rsvSerde));
        KStream<String, OrderAvro> stockOrderStream = builder.stream(stockOrderTopic);

        paymentOrders.join(
                        stockOrderStream,
                        orderManageService::confirm,
                        JoinWindows.of(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.String(), rsvSerde, rsvSerde))
                .peek((k, o) -> log.info("Output: {}", o))
                .to(orderTopic);

        return paymentOrders;
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafkaSender-");
        executor.initialize();
        return executor;
    }
}
