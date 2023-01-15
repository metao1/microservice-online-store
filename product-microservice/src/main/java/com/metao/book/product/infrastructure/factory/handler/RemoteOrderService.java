package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.kafka.RemoteKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteOrderService {

    private final RemoteKafkaService<String, OrderEvent> kafkaTemplate;
    private final NewTopic orderStockTopic;

    public void handle(OrderEvent orderEvent) {
        kafkaTemplate.sendToTopic(orderStockTopic.name(), orderEvent.getProductId(), orderEvent);
    }

}
