package com.metao.book.order.application.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExceptionHandlerKafkaStreamsBuilderFactoryBeanCustomizer implements StreamsBuilderFactoryBeanCustomizer {

    /**
     * Customize the {@link StreamsBuilderFactoryBean}.
     *
     * @param factoryBean the factory bean to customize
     */
    @Override
    public void customize(StreamsBuilderFactoryBean factoryBean) {
        factoryBean.setStreamsUncaughtExceptionHandler(e -> {
            log.warn("Uncaught Exception: {}", e.getMessage());
            return StreamThreadExceptionResponse.REPLACE_THREAD;
        });
    }
}
