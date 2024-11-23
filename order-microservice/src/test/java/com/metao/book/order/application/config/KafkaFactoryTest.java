package com.metao.book.order.application.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaFactoryTest {

    KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);

    KafkaFactory<String> kafkaFactory = new KafkaFactory<>(kafkaTemplate);

    @Test
    void testWhenSendingKafkaMessageThenKafkaTemplateSent() {
        //GIVEN
        var topic = "TOPIC";
        var key = "KEY";
        var message = "message";

        //WHEN
        kafkaFactory.subscribe();
        kafkaFactory.submit(topic, key, message);
        kafkaFactory.publish();

        //THEN
        verify(kafkaTemplate).send(topic, key, message);
    }

}