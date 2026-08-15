package com.metao.book.payment.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.transaction.KafkaAwareTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConsumerConfigTest {

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        this.config = new KafkaConsumerConfig(
            new KafkaProperties(),
            Mockito.mock(KafkaAwareTransactionManager.class)
        );
    }

    @Test
    void orderCreatedListenerFactoryUsesManualImmediateAck() {
        var factory = config.orderCreatedEventKafkaListenerContainerFactory(
            config.orderErrorHandler(config.orderDlqRecoverer(Mockito.mock(KafkaTemplate.class))),
            Mockito.mock(ConsumerFactory.class)
        );

        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        assertThat(ReflectionTestUtils.getField(factory, "commonErrorHandler")).isInstanceOf(DefaultErrorHandler.class);
    }
}
