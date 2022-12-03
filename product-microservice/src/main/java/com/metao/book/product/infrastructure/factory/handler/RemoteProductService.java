package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.shared.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteProductService {

    @Value("${kafka.topic.product}")
    private String productEventTopic;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @Transactional
    public void handle(ProductEvent productEvent) {
        kafkaTemplate.send(productEventTopic, productEvent.getProductId(), productEvent)
            .addCallback(result -> log.info("Sent: {}",
                result != null ? result.getProducerRecord().value() : null), ex -> {
            });

    }

}
