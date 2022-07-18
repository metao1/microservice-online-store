package com.metao.book.order.application.config;

import com.metao.book.order.domain.OrderManageService;
import com.order.microservice.avro.OrderAvro;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Slf4j
@Validated
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    private static final long WINDOW_SIZE = 10;
    private final OrderManageService orderManageService;
    private final KafkaProperties properties;

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
    SpecificAvroSerde<OrderAvro> orderAvroSerde() {
        var serde = new SpecificAvroSerde<OrderAvro>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    public KStream<Long, OrderAvro> stream(StreamsBuilder builder, SpecificAvroSerde<OrderAvro> orderSerde) {
        KStream<Long, OrderAvro> paymentOrders = builder
                .stream("payment-test1", Consumed.with(Serdes.Long(), orderSerde));
        KStream<Long, OrderAvro> stockOrderStream = builder
                .stream("stock-test1", Consumed.with(Serdes.Long(), orderSerde));

        paymentOrders.join(
                        stockOrderStream,
                        orderManageService::confirm,
                        JoinWindows.of(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.Long(), orderSerde, orderSerde))
                .peek((k, o) -> log.info("Output: {}", o))
                .to("order-test-3");

        return paymentOrders;
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
                        JoinWindows.of(Duration.ofSeconds(WINDOW_SIZE)),
                        StreamJoined.with(Serdes.String(), rsvSerde, rsvSerde))
                .peek((k, o) -> log.info("Output: {}", o))
                .to(orderTopic);

        return paymentOrders;
    }

}
