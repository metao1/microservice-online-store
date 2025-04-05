package com.metao.book.order.application.config;

import com.metao.book.shared.application.ObjectMapperConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaEventConfiguration.class)
@ImportAutoConfiguration(ObjectMapperConfig.class)
@EnableConfigurationProperties(KafkaEventConfiguration.class)
public class AppConfig {
}


