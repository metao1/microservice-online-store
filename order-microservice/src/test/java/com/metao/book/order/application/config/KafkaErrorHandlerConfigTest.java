package com.metao.book.order.application.config;

import static org.assertj.core.api.Assertions.assertThat;

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
        var recoverer = config.orderDlqRecoverer(Mockito.mock(KafkaTemplate.class));
        @SuppressWarnings("unchecked")
        var destinationResolver = (java.util.function.BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>)
            ReflectionTestUtils.getField(recoverer, "destinationResolver");
        var tp = destinationResolver.apply(new ConsumerRecord<>("order-payment", 0, 0L, "k", "v"), new RuntimeException("boom"));
        assertThat(tp).isEqualTo(new TopicPartition("order-payment.DLT", 0));
    }

    @Test
    void listenerFactorySetsErrorHandler() {
        var factory = config.orderPaymentEventKafkaListenerContainerFactory(
            config.orderErrorHandler(config.orderDlqRecoverer(Mockito.mock(KafkaTemplate.class))),
            Mockito.mock(ConsumerFactory.class)
        );
        var handler = ReflectionTestUtils.getField(factory, "commonErrorHandler");
        assertThat(handler).isInstanceOf(DefaultErrorHandler.class);
    }
}
