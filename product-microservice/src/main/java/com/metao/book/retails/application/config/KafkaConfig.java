package com.metao.book.retails.application.config;

// import org.apache.kafka.clients.producer.ProducerConfig;
// import org.apache.kafka.common.serialization.Serdes;
// import org.apache.kafka.streams.StreamsConfig;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.annotation.EnableKafka;
// import org.springframework.kafka.annotation.EnableKafkaStreams;
// import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
// import org.springframework.kafka.config.KafkaStreamsConfiguration;

// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// @EnableKafka
// @EnableKafkaStreams
// public class KafkaConfig {

// @Value(value = "${spring.kafka.bootstrap-servers}")
// private String bootstrapAddress;

// @Bean(name =
// KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
// KafkaStreamsConfiguration kStreamsConfig() {
// Map<String, Object> props = new HashMap<>();
// props.put(StreamsConfig.APPLICATION_ID_CONFIG, "retails-app");
// props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
// props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
// Serdes.String().getClass().getName());
// props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
// Serdes.String().getClass().getName());

// return new KafkaStreamsConfiguration(props);
// }

// }
