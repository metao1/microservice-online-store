package com.metao.book.order.application.config;

import com.metao.book.shared.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableKafka
@RequiredArgsConstructor
@Profile({"!test"})
@ImportAutoConfiguration(value = KafkaSerdesConfig.class)
public class OrderKafkaListenerConfig {

    @Transactional
    @KafkaListener(id = "order-kafka-id", topics = "${kafka.topic.order}")
    void onOrderListener(ConsumerRecord<String, OrderEvent> record) {
        var order = record.value();

    }
}
