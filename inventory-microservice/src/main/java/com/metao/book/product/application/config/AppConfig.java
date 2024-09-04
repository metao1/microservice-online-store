package com.metao.book.product.application.config;

import com.metao.book.shared.application.ObjectMapperConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(value = ObjectMapperConfig.class)
public class AppConfig {
}


