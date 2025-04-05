package com.metao.book.product.application.config;

import com.metao.book.shared.application.ObjectMapperConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({KafkaEventConfiguration.class, ObjectMapperConfig.class})
public class AppConfig {
}


