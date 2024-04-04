package com.metao.book.checkout.application.service;

import com.metao.book.shared.domain.order.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoteOrderService {

    private final KafkaTemplate<String, OrderEvent> orderTemplate;
    private final NewTopic orderPaymentTopic;

    public void handle(OrderEvent orderEvent) {
        orderTemplate.send(orderPaymentTopic.name(), orderEvent.productId(), orderEvent);
    }

}
