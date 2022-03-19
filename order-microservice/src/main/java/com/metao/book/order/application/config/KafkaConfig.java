package com.metao.book.order.application.config;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Slf4j
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class KafkaConfig {

    private static final long TIMEOUT = 10;
    private final OrderManageService orderManageService;

    @Bean
    public NewTopic orders(@Value("${stream.topic.order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean
    public NewTopic payment(@Value("${stream.topic.payment}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic output(@Value("${stream.topic.output}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic stock(@Value("${stream.topic.stock}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public KStream<Integer, OrderDTO> stream(@Value("${stream.topic.payment}") String paymentOrder,
            @Value("${stream.topic.stock}") String stockOrder,
            @Value("${stream.topic.output}") String orders,
            StreamsBuilder sb) {

        var orderJsonSerde = new JsonSerde<>(OrderDTO.class);
        var kStream = sb.stream(paymentOrder, Consumed.with(Serdes.Integer(), orderJsonSerde));

        kStream.join(
                sb.stream(stockOrder),
                orderManageService::confirm,
                JoinWindows.of(Duration.ofSeconds(TIMEOUT)),
                StreamJoined.with(Serdes.Integer(), orderJsonSerde, orderJsonSerde))
                .peek((k, o) -> log.info("output :{}", o))
                .to(orders);

        return kStream;
    }

    @Bean
    public KTable<Integer, OrderDTO> table(@Value("${stream.topic.output}") String topic, StreamsBuilder sb) {
        var store = Stores.persistentKeyValueStore(topic);
        var orderJsonSerde = new JsonSerde<>(OrderDTO.class);
        var stream = sb.stream(topic, Consumed.with(Serdes.Integer(), orderJsonSerde));
        return stream.toTable(Materialized.<Integer, OrderDTO>as(store)
                .withKeySerde(Serdes.Integer())
                .withValueSerde(orderJsonSerde));
    }
}
