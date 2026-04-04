package com.metao.kafka;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import jakarta.annotation.PostConstruct;
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Getter
@Setter
@AutoConfiguration
@ConfigurationProperties("kafka")
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaEventConfiguration {

    private Map<String, String> classToTopicMap;
    private Map<String, KafkaPropertyTopic> topic;

    @PostConstruct
    public void init() {
        classToTopicMap = new HashMap<>();
        topic.values().forEach(topicConfig ->
            classToTopicMap.put(topicConfig.classPath(), topicConfig.name())
        );
    }

    record KafkaPropertyTopic(String id, String name, String groupId, String classPath) {

    }
    @Bean
    @Primary
    public <K, V> KafkaTemplate<K, V> kafkaTemplate(
        ProducerFactory<K, V> producerFactory
    ) {
        var template = new KafkaTemplate<>(producerFactory);
        template.setAllowNonTransactional(true);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    <K, V> ProducerFactory<K, V> producerFactory(
        KafkaProperties kafkaProperties,
        @Value("${spring.kafka.properties.schema.registry.url}") String schemaRegistryUrl,
        @Value("${spring.kafka.producer.transaction-id-prefix:}") String transactionIdPrefix
    ) {
        var bootstrapServers = kafkaProperties.getBootstrapServers();
        var ps = kafkaProperties.getProperties();
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.putAll(ps);

        var factory = new DefaultKafkaProducerFactory<K, V>(props);
        if (transactionIdPrefix != null && !transactionIdPrefix.isBlank()) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return factory;
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
        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }

    @Bean
    public <T> ConsumerFactory<String, T> consumerFactory(
        KafkaProperties kafkaProperties
    ) {
        var bootstrapServers = kafkaProperties.getBootstrapServers();
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

}
