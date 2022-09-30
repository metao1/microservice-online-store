package com.metao.book.order.application.config;

import com.metao.book.order.application.dto.ProductDTO;
import com.metao.book.order.domain.OrderManageService;
import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.ProductEvent;
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
import org.springframework.core.convert.ConversionService;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Objects;

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

    @Value("${kafka.topic.payment}")
    String paymentTopic;

    @Value("${kafka.topic.order}")
    String orderTopic;

    @Value("${kafka.topic.stock}")
    String stockTopic;

    @Value("${spring.kafka.properties.schema.registry.url}")
    String srUrl;

    @Value("${spring.kafka.properties.basic.auth.credentials.source}")
    String crSource;

    @Value("${spring.kafka.properties.schema.registry.basic.auth.user.info}")
    String authUser;

    @Bean
    public NewTopic orders(@Value("${kafka.stream.topic.order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    public NewTopic payment(@Value("${kafka.stream.topic.payment-order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
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
                .stream(paymentTopic, Consumed.with(Serdes.Long(), orderSerde));
        KStream<Long, OrderAvro> stockOrderStream = builder
                .stream(stockTopic, Consumed.with(Serdes.Long(), orderSerde));

        paymentOrders.join(
                        stockOrderStream,
                        orderManageService::confirm,
                        JoinWindows.of(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.Long(), orderSerde, orderSerde))
                .peek((k, o) -> log.info("Output: {}", o))
                .to(orderTopic);

        return paymentOrders;
    }

    @Bean
    public KTable<String, ProductDTO> productTable(StreamsBuilder sb, ConversionService conversionService,
                                                   SpecificAvroSerde<ProductEvent> serde) {
        return sb.table(orderTopic, Consumed.with(Serdes.String(), serde))
                .mapValues(val -> Objects.requireNonNull(conversionService.convert(val, ProductDTO.class)));
    }

    @Bean
    public KTable<String, OrderAvro> table(StreamsBuilder sb, SpecificAvroSerde<OrderAvro> specificAvroSerde) {
        var store = Stores.persistentKeyValueStore(orderTopic);
        var stream = sb.stream(orderTopic, Consumed.with(Serdes.String(), specificAvroSerde));
        return stream.toTable(Materialized.<String, OrderAvro>as(store)
                .withKeySerde(Serdes.String())
                .withValueSerde(specificAvroSerde));
    }

    @Bean
    public KStream<String, OrderAvro> stream(StreamsBuilder builder) {
        var rsvSerde = new SpecificAvroSerde<OrderAvro>();
        KStream<String, OrderAvro> paymentOrders = builder
                .stream(paymentTopic, Consumed.with(Serdes.String(), rsvSerde));
        KStream<String, OrderAvro> stockOrderStream = builder.stream(stockTopic);

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
