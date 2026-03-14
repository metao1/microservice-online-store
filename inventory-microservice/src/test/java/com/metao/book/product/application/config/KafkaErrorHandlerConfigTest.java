package com.metao.book.product.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiFunction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaErrorHandlerConfigTest {

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        this.config = new KafkaConsumerConfig(new KafkaProperties());
    }

    @Test
    void dltRecovererAppendsSuffix() {
        var recoverer = config.productDlqRecoverer(Mockito.mock(KafkaTemplate.class));
        @SuppressWarnings("unchecked")
        var destinationResolver = (BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>)
            ReflectionTestUtils.getField(recoverer, "destinationResolver");
        var tp = destinationResolver.apply(new ConsumerRecord<>("product-created", 0, 0L, "k", "v"),
            new RuntimeException("boom"));
        assertThat(tp).isEqualTo(new TopicPartition("product-created.DLT", 0));
    }

    @Test
    void errorHandlerIsConfiguredWithDlq() {
        var recoverer = config.productDlqRecoverer(Mockito.mock(KafkaTemplate.class));
        var handler = config.productErrorHandler(recoverer);
        assertThat(handler).isNotNull();
    }

    @Test
    void listenerFactorySetsErrorHandler() {
        var factory = config.productCreatedEventKafkaListenerContainerFactory(
            Mockito.mock(ConsumerFactory.class),
            config.productErrorHandler(
                config.productDlqRecoverer(Mockito.mock(KafkaTemplate.class)))
        );
        var handler = ReflectionTestUtils.getField(factory, "commonErrorHandler");
        assertThat(handler).isInstanceOf(DefaultErrorHandler.class);
    }
}
