package com.metao.product.retails.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Converter {

    @Bean
    public Gson provideGson() {
        return new Gson();
    }

}
