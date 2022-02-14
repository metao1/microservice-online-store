package com.metao.product.retails.application.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean
    public Gson provideGson() {
        return new Gson();
    }

}
