package com.metao.product.retails.application.config;

import com.google.gson.Gson;
import com.metao.product.retails.application.dto.ProductDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean
    public Gson provideGson() {
        return new Gson();
    }
}
