package com.metao.book.order.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionHandlerKafkaStreamsBuilderFactoryBeanCustomizer implements StreamsBuilderFactoryBeanCustomizer {

        /**
         * {@inheritDoc}
         */
        @Override
        public void customize(StreamsBuilderFactoryBean factoryBean) {
                factoryBean.setUncaughtExceptionHandler((t, e) -> log.warn("Uncaught Exception for Thread: {}", t, e));
        }
}
