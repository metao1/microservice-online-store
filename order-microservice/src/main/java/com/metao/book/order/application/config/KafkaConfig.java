package com.metao.book.order.application.config;

import com.metao.book.order.domain.OrderManageService;
import com.metao.book.shared.OrderAvro;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Configuration
//@EnableKafkaStreams
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

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
    public NewTopic orders(@Value("${kafka.topic.order}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(3)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

    @Bean
    public NewTopic payment(@Value("${kafka.topic.payment}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(3)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

    @Bean
    public NewTopic stock(@Value("${kafka.topic.stock}") String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(3)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

    @Bean
    SpecificAvroSerde<OrderAvro> orderAvroSerde() {
        var serde = new SpecificAvroSerde<OrderAvro>();
        serde.configure(properties.getProperties(), false);
        return serde;
    }

//    @Bean
//    public KStream<Long, OrderAvro> stream(StreamsBuilder builder, SpecificAvroSerde<OrderAvro> orderSerde) {
//        KStream<Long, OrderAvro> paymentOrders = builder
//                .stream(paymentTopic, Consumed.with(Serdes.Long(), orderSerde));
//        KStream<Long, OrderAvro> stockOrderStream = builder
//                .stream(stockTopic, Consumed.with(Serdes.Long(), orderSerde));
//        paymentOrders.join(
//                        stockOrderStream,
//                        orderManageService::confirm,
//                        JoinWindows.of(Duration.ofSeconds(10)),
//                        StreamJoined.with(Serdes.Long(), orderSerde, orderSerde))
//                .peek((k, o) -> log.info("Output: {}", o))
//                .to(orderTopic);
//
//        return paymentOrders;
//    }
//
//    @Bean
//    public KTable<String, ProductDTO> productTable(
//        StreamsBuilder sb, ConversionService conversionService,
//        SpecificAvroSerde<ProductEvent> serde
//    ) {
//        return sb.table(orderTopic, Consumed.with(Serdes.String(), serde))
//            .mapValues(val -> Objects.requireNonNull(conversionService.convert(val, ProductDTO.class)));
//    }
//
//    @Bean
//    SpecificAvroSerde<ProductEvent> productEventSpecificAvroSerde(KafkaProperties kafkaProperties) {
//        var result = new SpecificAvroSerde<ProductEvent>();
//        result.configure(kafkaProperties.getProperties(), false);
//        return result;
//    }
//
//    @Bean
//    public KTable<String, OrderAvro> table(StreamsBuilder sb, SpecificAvroSerde<OrderAvro> specificAvroSerde) {
//        var store = Stores.persistentKeyValueStore(orderTopic);
//        var stream = sb.stream(orderTopic, Consumed.with(Serdes.String(), specificAvroSerde));
//        return stream.toTable(Materialized.<String, OrderAvro>as(store)
//            .withKeySerde(Serdes.String())
//            .withValueSerde(specificAvroSerde));
//    }

}
