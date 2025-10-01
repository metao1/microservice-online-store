package com.metao.book.order.application.config;

import com.metao.book.shared.application.ObjectMapperConfig;
import com.metao.book.shared.domain.base.DelegatingDomainEventTranslator;
import com.metao.kafka.KafkaEventConfiguration;
import com.metao.kafka.KafkaEventHandler;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ImportAutoConfiguration(ObjectMapperConfig.class)
@Import({KafkaEventConfiguration.class, KafkaEventHandler.class, DelegatingDomainEventTranslator.class})
public class AppConfig {
}
