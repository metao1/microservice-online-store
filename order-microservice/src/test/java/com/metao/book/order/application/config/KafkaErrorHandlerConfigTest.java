package com.metao.book.order.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
class KafkaErrorHandlerConfigTest {

    @Autowired
    ConcurrentKafkaListenerContainerFactory<String, ?> orderPaymentEventKafkaListenerContainerFactory;

    @Test
    void errorHandlerRoutesToDltWithSuffix() {
        var handler = (DefaultErrorHandler) orderPaymentEventKafkaListenerContainerFactory.getCommonErrorHandler();
        assertThat(handler).isNotNull();

        @SuppressWarnings("unchecked")
        var recoverer = (java.util.function.BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>)
            ReflectionTestUtils.getField(handler, "recoverer");

        var record = new ConsumerRecord<>("order-payment", 0, 0L, "key", "value");
        var tp = recoverer.apply(record, new RuntimeException("boom"));
        assertThat(tp.topic()).isEqualTo("order-payment.DLT");
    }
}
