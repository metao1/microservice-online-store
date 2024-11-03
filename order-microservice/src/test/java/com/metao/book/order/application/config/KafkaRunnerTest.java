package com.metao.book.order.application.config;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaRunnerTest {

    KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);

    KafkaRunner<String> kafkaRunner = new KafkaRunner<>(kafkaTemplate);

    @Test
    void testWhenSendingKafkaMessageThenKafkaTemplateSent() {
        //GIVEN
        var topic = "TOPIC";
        var key = "KEY";
        var message = "message";

        //WHEN
        kafkaRunner.subscribe();
        kafkaRunner.submit(topic, key, message);
        kafkaRunner.publish();

        //THEN
        verify(kafkaTemplate).send(topic, key, message);
    }

}