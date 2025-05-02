package com.metao.book.order.application.config;

import com.metao.book.shared.application.kafka.KafkaFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@ConditionalOnProperty(
    havingValue = "true",
    name = {"kafka.enabled"}
)
@ConfigurationProperties("kafka")
public class KafkaEventConfiguration {

    private boolean enabled;
    private Map<String, KafkaPropertyTopic> topics;

    @Bean
    public Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap(
        final Map<String, KafkaFactory<?>> kafkaFactories
    ) {
        return getTopics().entrySet().stream().map((entry) ->
                kafkaFactories.computeIfPresent(entry.getKey(), (k, kafkaFactory) -> {
                    kafkaFactory.setTopic(entry.getValue().name());
                    return kafkaFactory;
                }))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(KafkaFactory::getType, Function.identity()));
    }

    @Bean
    KafkaFactoryBuilder kafkaFactoryBuilder(ProducerFactory<String, Object> producerFactory) {
        return new KafkaFactoryBuilder(producerFactory);
    }

    record KafkaPropertyTopic(String id, String name, String groupId, String classPath) {

    }

    @Configuration
    @RequiredArgsConstructor
    static class KafkaFactoryBuilder {

        private final ProducerFactory<String, Object> producerFactory;

        public <V> KafkaFactory<V> createFactory(Class<V> valueType) {
            ProducerFactory<String, V> typedProducerFactory = (ProducerFactory<String, V>) producerFactory;
            KafkaTemplate<String, V> kafkaTemplate = new KafkaTemplate<>(typedProducerFactory);
            return new KafkaFactory<>(valueType, kafkaTemplate);
        }
    }

    @Configuration
    class KafkaFactoryConfig {

        @Bean
        public Map<String, KafkaFactory<?>> kafkaFactories(KafkaFactoryBuilder kafkaFactoryBuilder)
            throws ClassNotFoundException {
            Map<String, KafkaFactory<?>> factories = new HashMap<>();

            for (KafkaPropertyTopic topic : KafkaEventConfiguration.this.topics.values()) {
                factories.put(topic.id, kafkaFactoryBuilder.createFactory(Class.forName(topic.classPath)));
            }

            return factories;
        }
    }
}
