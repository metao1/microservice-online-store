package com.metao.book.checkout.application.config;

import com.metao.book.shared.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
@ImportAutoConfiguration(value = KafkaSerdesConfig.class)
public class CheckoutKafkaListenerConfig {

    @Transactional
    @KafkaListener(id = "checkout-order-id", topics = "${kafka.topic.order}")
    public void orderKafkaListener(ConsumerRecord<String, OrderEvent> record) {
        var orderEvent = record.value();

    }
}
