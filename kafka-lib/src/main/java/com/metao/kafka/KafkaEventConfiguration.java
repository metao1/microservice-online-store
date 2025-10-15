package com.metao.kafka;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Getter
@Setter
@EnableKafka
@AutoConfiguration
@ConfigurationProperties("kafka")
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaEventConfiguration {

    private Map<String, KafkaPropertyTopic> topic;

    record KafkaPropertyTopic(String id, String name, String groupId, String classPath) {

    }

    @Bean
    @Primary
    public <K, V> KafkaTemplate<K, V> kafkaTemplate(final ProducerFactory<K, V> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    <K, V> ProducerFactory<K, V> producerFactory(
        KafkaProperties kafkaProperties,
        @Value("${spring.kafka.properties.schema.registry.url}") String schemaRegistryUrl
    ) {
        var bootstrapServers = kafkaProperties.getBootstrapServers();
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer.class);
        configProps.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public <K, V> ConcurrentKafkaListenerContainerFactory<K, V> kafkaListenerContainerFactory(
        KafkaProperties kafkaProperties,
        @Value("${spring.kafka.properties.schema.registry.url}") String schemaRegistryUrl
    ) {
        var ps = kafkaProperties.getProperties();
        var props = new HashMap<String, Object>();

        var bootstrapServers = kafkaProperties.getBootstrapServers();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // Increase poll interval
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Reduce poll records
        props.put("schema.registry.url", schemaRegistryUrl);

        props.putAll(ps);

        var consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        var factory = new ConcurrentKafkaListenerContainerFactory<K, V>();

        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);

        return factory;
    }
}
