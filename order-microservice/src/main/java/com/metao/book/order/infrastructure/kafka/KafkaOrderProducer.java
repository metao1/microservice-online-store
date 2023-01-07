package com.metao.book.order.infrastructure.kafka;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.kafka.RemoteKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ComponentScan(basePackageClasses = RemoteKafkaService.class)
public class KafkaOrderProducer {

    private final RemoteKafkaService<String, OrderEvent> kafkaTemplate;
    private final NewTopic orderTopic;

    @Transactional
    public void handle(OrderEvent orderEvent) {
        kafkaTemplate.sendToTopic(orderTopic.name(), orderEvent.getProductId(), orderEvent);
    }

}
