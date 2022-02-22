package com.metao.product.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }
}
