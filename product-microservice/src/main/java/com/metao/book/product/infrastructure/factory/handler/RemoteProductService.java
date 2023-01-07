package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.shared.ProductEvent;
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
public class RemoteProductService {

    private final RemoteKafkaService<String, ProductEvent> kafkaTemplate;
    private final NewTopic productTopic;

    @Transactional
    public void handle(ProductEvent productEvent) {
        kafkaTemplate.sendToTopic(productTopic.name(), productEvent.getProductId(), productEvent);
    }

}
