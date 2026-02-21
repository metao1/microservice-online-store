package com.metao.book.order.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.shared.OrderPaymentEvent;
import com.metao.kafka.KafkaClientProperties;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaErrorHandlerConfigTest {

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        KafkaClientProperties props = new KafkaClientProperties();
        props.setBootstrapServers(List.of("localhost:9092"));
        this.config = new KafkaConsumerConfig(props);
        // inject bootstrap servers field
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
    }

    @Test
    void dltRecovererAppendsSuffix() {
        var recoverer = config.orderDlqRecoverer(Mockito.mock(org.springframework.kafka.core.KafkaTemplate.class));
        @SuppressWarnings("unchecked")
        var destinationResolver = (java.util.function.BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>)
            ReflectionTestUtils.getField(recoverer, "destinationResolver");
        var tp = destinationResolver.apply(new ConsumerRecord<>("order-payment", 0, 0L, "k", "v"), new RuntimeException("boom"));
        assertThat(tp).isEqualTo(new TopicPartition("order-payment.DLT", 0));
    }

    @Test
    void listenerFactorySetsErrorHandler() {
        var factory = config.orderPaymentEventKafkaListenerContainerFactory(
            config.orderErrorHandler(config.orderDlqRecoverer(Mockito.mock(org.springframework.kafka.core.KafkaTemplate.class)))
        );
        var handler = ReflectionTestUtils.getField(factory, "commonErrorHandler");
        assertThat(handler).isInstanceOf(DefaultErrorHandler.class);
    }
}
