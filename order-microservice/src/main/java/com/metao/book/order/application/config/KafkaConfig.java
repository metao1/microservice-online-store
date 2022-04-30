package com.metao.book.order.application.config;

import com.metao.book.order.domain.OrderManageService;
import com.order.microservice.avro.OrderAvro;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Configuration
//@EnableKafkaStreams
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    private static final long TIMEOUT = 10;
    private final OrderManageService orderManageService;
    private final KafkaProperties properties;

    @Bean("order")
    public NewTopic orders(@Value("${kafka.stream.topic.order}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                .compact()
                .build();
    }

    @Bean("payments")
    public NewTopic payment(@Value("${kafka.stream.topic.payment}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean("output")
    public NewTopic output(@Value("${kafka.stream.topic.output}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean("stock")
    public NewTopic stock(@Value("${kafka.stream.topic.stock}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public ProducerFactory<String, OrderAvro> devKeyValueSerializerKafkaProducerFactory() {

        // if you have trouble with i.e. the Confluent Schema registry write just JSON -
        // this is much easier to reprocess
        var configProps = this.properties.buildProducerProperties();
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                KafkaAvroSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * @see KafkaAutoConfiguration#kafkaTemplate
     * (
     * org.springframework.kafka.core.ProducerFactory,
     * org.springframework.kafka.support.ProducerListener,
     * org.springframework.beans.factory.ObjectProvider
     * )
     */
    @Bean
    public KafkaTemplate<String, OrderAvro> devKafkaTemplate(
            ObjectProvider<RecordMessageConverter> messageConverter,
            ProducerFactory<String, OrderAvro> devKeyValueSerializerKafkaProducerFactory) {

        var kafkaTemplate = new KafkaTemplate<>(devKeyValueSerializerKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        return kafkaTemplate;
    }

//    @Bean
//    SpecificAvroSerde<OrderAvro> orderAvroSerde() {
//        var serde = new SpecificAvroSerde<OrderAvro>();
//        serde.configure(properties.getProperties(), false);
//        return serde;
//    }

//    @Bean
//    public KStream<Integer, OrderAvro> stream(@Value("${kafka.stream.topic.order}") String paymentOrder,
//                                              @Value("${kafka.stream.topic.stock}") String stockOrder,
//                                              @Value("${kafka.stream.topic.output}") String orders,
//                                              SpecificAvroSerde<OrderAvro> orderAvroSerde,
//                                              StreamsBuilder sb) {
//
//        var kStream = sb.stream(paymentOrder, Consumed.with(Serdes.Integer(), orderAvroSerde));
//
//        kStream.join(
//                        sb.stream(stockOrder),
//                        orderManageService::confirm,
//                        JoinWindows.of(Duration.ofSeconds(TIMEOUT)),
//                        StreamJoined.with(Serdes.Integer(), orderAvroSerde, orderAvroSerde))
//                .peek((k, o) -> log.info("output :{}", o))
//                .to(orders);
//
//        return kStream;
//    }

//    @Bean
//    public KTable<String, OrderAvro> table(@Value("${kafka.stream.topic.order}") String orderTopic,
//                                            StreamsBuilder sb) {
//        var store = Stores.persistentKeyValueStore(orderTopic);
//        var orderSerde = new SpecificAvroSerde<OrderAvro>();
//        var stream = sb.stream(orderTopic, Consumed.with(Serdes.String(), orderSerde));
//        return stream.toTable(Materialized.<String, OrderAvro>as(store)
//                .withKeySerde(Serdes.String())
//                .withValueSerde(orderSerde));
//    }
}
