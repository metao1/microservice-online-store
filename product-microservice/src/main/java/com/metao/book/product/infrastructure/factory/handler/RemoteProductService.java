package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.service.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Service
@EnableTransactionManagement
@RequiredArgsConstructor
public class RemoteProductService {

    private final RemoteKafkaService<String, ProductEvent> kafkaTemplate;
    private final ProductValidator productValidator;
    private final NewTopic productTopic;

    public void sendToKafka(ProductEvent productEvent) {
        productValidator.validate(productEvent);
        sendToKafka(productTopic.name(), productEvent);
    }

    public void sendToKafka(String topic, ProductEvent productEvent) {
        kafkaTemplate.sendToTopic(topic, productEvent.getProductId(), productEvent);
    }

}
