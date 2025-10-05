package com.metao.kafka;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Getter
@Setter
@EnableKafka
@Configuration
@ConfigurationProperties("kafka")
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaEventConfiguration {

    private Map<String, KafkaPropertyTopic> topic;

    record KafkaPropertyTopic(String id, String name, String groupId, String classPath) {

    }

    @Bean
    public Map<String, KafkaTemplate<?, Object>> kafkaFactoryMap(
        final ProducerFactory<?, Object> producerFactory
    ) {
        return topic.values()
            .stream()
            .map(kafkaPropertyTopic -> {
                    KafkaTemplate<?, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);
                    kafkaTemplate.setDefaultTopic(kafkaPropertyTopic.name());
                    try {
                        Class<?> eventClass = Class.forName(kafkaPropertyTopic.classPath());
                        return Map.entry(eventClass.getName(), kafkaTemplate);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Failed to load event class: " + kafkaPropertyTopic.classPath(), e);
                    }
                }
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Bean
    ProducerFactory<?, Object> producerFactory(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
        @Value("${spring.kafka.properties.schema.registry.url}") String schemaRegistryUrl
    ) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer.class);
        configProps.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(
        List<String> bootstrapServers,
        KafkaProperties kafkaProperties
    ) {
        var ps = kafkaProperties.getProperties();
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // Increase poll interval
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Reduce poll records

        props.putAll(ps);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> kafkaListenerContainerFactory(
        KafkaProperties kafkaProperties
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(createConsumerFactory(kafkaProperties.getBootstrapServers(), kafkaProperties));
        factory.setConcurrency(1);

        return factory;
    }
}
