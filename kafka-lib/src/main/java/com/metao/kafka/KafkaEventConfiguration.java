package com.metao.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Getter
@Setter
@Configuration
@EnableAutoConfiguration
@ConfigurationProperties("kafka")
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class KafkaEventConfiguration {

    private boolean enabled;
    private Map<String, KafkaPropertyTopic> topic;

    @Bean
    public Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap(
        final Map<String, KafkaFactory<?>> kafkaFactories
    ) {
        return topic.values().stream().map(
                kafkaPropertyTopic -> kafkaFactories.computeIfPresent(kafkaPropertyTopic.id(), (k, kafkaFactory) -> {
                    kafkaFactory.setTopic(kafkaPropertyTopic.name());
                    return kafkaFactory;
                })
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(KafkaFactory::getType, Function.identity()));
    }

    @Bean
    public Map<String, KafkaFactory<?>> kafkaFactories(KafkaFactoryBuilder kafkaFactoryBuilder)
        throws ClassNotFoundException {
        Map<String, KafkaFactory<?>> factories = new HashMap<>();

        for (KafkaPropertyTopic topic : topic.values()) {
            factories.put(topic.id(), kafkaFactoryBuilder.createFactory(Class.forName(topic.classPath())));
        }

        return factories;
    }

    @Bean
    KafkaFactoryBuilder kafkaFactoryBuilder(ProducerFactory<String, Object> producerFactory) {
        return new KafkaFactoryBuilder(producerFactory);
    }

    record KafkaPropertyTopic(String id, String name, String groupId, String classPath) {

    }

    @RequiredArgsConstructor
    static class KafkaFactoryBuilder {

        private final ProducerFactory<String, Object> producerFactory;

        public <V> KafkaFactory<V> createFactory(Class<V> valueType) {
            ProducerFactory<String, V> typedProducerFactory = (ProducerFactory<String, V>) producerFactory;
            KafkaTemplate<String, V> kafkaTemplate = new KafkaTemplate<>(typedProducerFactory);
            return new KafkaFactory<>(valueType, kafkaTemplate);
        }
    }
}
