package com.metao.book.order.application.config;

import static com.metao.book.shared.kafka.StreamsUtils.createTopic;

import com.metao.book.shared.kafka.KafkaMessageDeserializer;
import com.metao.book.shared.kafka.RemoteKafkaService;
import com.metao.book.shared.kafka.StreamDeserializationErrorHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
@ComponentScan(basePackageClasses = {RemoteKafkaService.class, StreamDeserializationErrorHandler.class})
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.order}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderStockTopic(@Value("${kafka.topic.order-stock}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic orderPaymentTopic(@Value("${kafka.topic.order-payment}") String topic) {
        return createTopic(topic);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory(
        ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
        ConsumerFactory<Object, Object> kafkaConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setCommonErrorHandler(new KafkaMessageDeserializer()); // <<<<<<
        return factory;
    }

}
