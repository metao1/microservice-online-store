package com.metao.book.order.application.config;

import com.metao.book.shared.application.ObjectMapperConfig;
import com.metao.kafka.KafkaEventConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaEventConfiguration.class)
@ImportAutoConfiguration(ObjectMapperConfig.class)
public class AppConfig {
}
