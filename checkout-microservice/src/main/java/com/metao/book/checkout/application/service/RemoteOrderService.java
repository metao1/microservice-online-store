package com.metao.book.checkout.application.service;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.kafka.RemoteKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ComponentScan(basePackageClasses = RemoteKafkaService.class)
public class RemoteOrderService {

    private final RemoteKafkaService<String, OrderEvent> kafkaTemplate;
    private final NewTopic orderPaymentTopic;

    public void handle(OrderEvent orderEvent) {
        kafkaTemplate.sendToTopic(orderPaymentTopic.name(), orderEvent.getProductId(), orderEvent);
    }

}
