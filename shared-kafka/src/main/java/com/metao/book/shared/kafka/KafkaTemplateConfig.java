package com.metao.book.shared.kafka;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaTemplateConfig {
/*
    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
    public <S, T> KafkaTemplate<S, T> kafkaTemplate(
        ObjectProvider<RecordMessageConverter> messageConverter,
        KafkaProperties kafkaProperties
    ) {
        var kafkaTemplate = new KafkaTemplate<S, T>(defaultKafkaProducerFactory(kafkaProperties));
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        return kafkaTemplate;
    }

    private <S, T> ProducerFactory<S, T> defaultKafkaProducerFactory(KafkaProperties properties) {
        var configProps = properties.buildProducerProperties();
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, "0");
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, "0");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        return new DefaultKafkaProducerFactory<>(configProps);
    }*/
}
