package com.metao.book.order.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
@ComponentScan
public class ConverterConfiguration {

    @Bean
    public ConfigurableConversionService conversionService(Converter<?, ?>[] converters) {
        var result = new DefaultConversionService();
        for (Converter<?, ?> c : converters) {
            result.addConverter(c);
        }
        return result;
    }
}
