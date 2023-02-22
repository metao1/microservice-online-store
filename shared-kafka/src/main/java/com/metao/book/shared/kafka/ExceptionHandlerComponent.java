package com.metao.book.shared.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExceptionHandlerComponent {

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsCustomizer() {
        return new StreamsBuilderFactoryBeanConfigurer() {

            @Override
            public int getOrder() {
                return Integer.MAX_VALUE - 10000;
            }

            @Override
            public void configure(StreamsBuilderFactoryBean factoryBean) {
                factoryBean.setStreamsUncaughtExceptionHandler(getStreamsUncaughtExceptionHandler());
            }
        };
    }

    private StreamsUncaughtExceptionHandler getStreamsUncaughtExceptionHandler() {
        return exception -> {
            Throwable cause = exception.getCause();
            if (cause.getClass().equals(Exception.class)) {
                log.error(cause.getMessage());
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
            }
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
        };
    }

}
