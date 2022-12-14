package com.metao.book.order.application.config;

import com.metao.book.order.application.dto.ProductDTO;
import com.metao.book.order.domain.OrderManageService;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.ProductEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Slf4j
@EnableKafkaStreams
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaStreamConfig {

    private final KafkaProperties properties;
    private final OrderManageService orderManageService;

    @Value("${kafka.topic.payment}")
    String paymentTopic;

    @Value("${kafka.topic.order}")
    String orderTopic;

    @Value("${kafka.topic.product}")
    String productTopic;

    @Bean
    SpecificAvroSerde<ProductEvent> productEventSpecificAvroSerde(KafkaProperties kafkaProperties) {
        var result = new SpecificAvroSerde<ProductEvent>();
        result.configure(kafkaProperties.getProperties(), false);
        return result;
    }

    @Bean
    SpecificAvroSerde<OrderEvent> OrderEventSerde() {
        var serde = new SpecificAvroSerde<OrderEvent>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

    @Bean
    public KStream<Long, OrderEvent> stream(StreamsBuilder builder, SpecificAvroSerde<OrderEvent> orderSerde) {
        KStream<Long, OrderEvent> paymentOrders = builder
            .stream(paymentTopic, Consumed.with(Serdes.Long(), orderSerde));
        KStream<Long, OrderEvent> stockOrderStream = builder
            .stream(productTopic, Consumed.with(Serdes.Long(), orderSerde));
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
    public KTable<String, ProductDTO> productTable(
        StreamsBuilder sb, ConversionService conversionService,
        SpecificAvroSerde<ProductEvent> serde
    ) {
        return sb.table(orderTopic, Consumed.with(Serdes.String(), serde))
            .mapValues(val -> Objects.requireNonNull(conversionService.convert(val, ProductDTO.class)));
    }

    @Bean
    public KTable<String, OrderEvent> table(StreamsBuilder sb, SpecificAvroSerde<OrderEvent> specificAvroSerde) {
        var store = Stores.persistentKeyValueStore(orderTopic);
        var stream = sb.stream(orderTopic, Consumed.with(Serdes.String(), specificAvroSerde));
        return stream.toTable(Materialized.<String, OrderEvent>as(store)
            .withKeySerde(Serdes.String())
            .withValueSerde(specificAvroSerde));
    }

}
