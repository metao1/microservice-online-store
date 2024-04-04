package com.metao.book.product.application.config;

import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.domain.event.ProductCreatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableKafka
@Profile({"!test"})
@RequiredArgsConstructor
public class ProductKafkaListenerComponent {

    private final ProductRepository productRepository;
    private final ProductMapService productMapService;

    @KafkaListener(id = "${kafka.topic.product}",
        topics = "${kafka.topic.product}",
        groupId = "${kafka.topic.product}")
    public void onProductEvent(ConsumerRecord<String, ProductCreatedEvent> event) {
        var productCreatedEvent = event.value();
        log.info("Consumed productCreatedEvent -> {}", productCreatedEvent);
        var productEvent = productCreatedEvent.productEvent();
        StageProcessor.accept(productEvent)
            .thenApply(productMapService::toEntity)
            .acceptExceptionally((productEntity, exp) -> {
                if (exp != null) {
                    log.error("while saving entity {}, error:{}", productEntity, exp.getMessage());
                } else {
                    productRepository.save(productEntity);
                }
            });
    }

}
