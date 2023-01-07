package com.metao.book.product.application.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaSerdesConfig {

    @Bean
    CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, false);
    }

}
