package com.metao.book.shared.application.kafka;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableAutoConfiguration
public class EventConfiguration {

    @Getter
    @Setter
    @RequiredArgsConstructor
    @ConfigurationProperties("kafka")
    @ConditionalOnProperty(havingValue = "true", name = "isEnabled")
    public static class KafkaTopicProperties {

        private Map<String, KafkaPropertyTopic> topics;

        record KafkaPropertyTopic(String id, String name, String groupId) {

        }
    }

    @Configuration
    @RequiredArgsConstructor
    @Import(KafkaFactoryConfig.class)
    @EnableConfigurationProperties({KafkaTopicProperties.class})
    public static class AppConfig {

        @Bean
        public Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap(
            final KafkaTopicProperties kafkaTopicProperties,
            final Map<String, KafkaFactory<?>> kafkaFactories
        ) {
            return kafkaTopicProperties.getTopics().entrySet().stream().map((entry) ->
                    kafkaFactories.computeIfPresent(entry.getKey(), (k, kafkaFactory) -> {
                        kafkaFactory.setTopic(entry.getValue().name());
                        return kafkaFactory;
                    }))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(KafkaFactory::getType, Function.identity()));
        }
    }

    @Getter
    public enum KafkaTopics {
        ORDER_UPDATED("order-updated", OrderUpdatedEvent.class),
        ORDER_CREATED("order-created", OrderCreatedEvent.class),
        PRODUCT_UPDATED("product-updated", ProductUpdatedEvent.class);

        private final String topicId;
        private final Class<?> clazz;

        KafkaTopics(String topicId, Class<?> clazz) {
            this.topicId = topicId;
            this.clazz = clazz;
        }
    }

    @RequiredArgsConstructor
    static class KafkaFactoryBuilder {

        private final ProducerFactory<String, Object> producerFactory;

        @SuppressWarnings("unchecked")
        public <V> KafkaFactory<V> createFactory(Class<V> valueType) {
            ProducerFactory<String, V> typedProducerFactory = (ProducerFactory<String, V>) producerFactory;
            KafkaTemplate<String, V> kafkaTemplate = new KafkaTemplate<>(typedProducerFactory);
            return new KafkaFactory<>(valueType, kafkaTemplate);
        }
    }

    @Bean
    KafkaFactoryBuilder kafkaFactoryBuilder(ProducerFactory<String, Object> producerFactory) {
        return new KafkaFactoryBuilder(producerFactory);
    }

    @Configuration
    @RequiredArgsConstructor
    static class KafkaFactoryConfig {

        private final KafkaFactoryBuilder kafkaFactoryBuilder;

        @Bean
        public Map<String, KafkaFactory<?>> kafkaFactories() {
            Map<String, KafkaFactory<?>> factories = new HashMap<>();
            for (KafkaTopics topic : KafkaTopics.values()) {
                factories.put(topic.getTopicId(), kafkaFactoryBuilder.createFactory(topic.getClazz()));
            }
            return factories;
        }
    }
}
