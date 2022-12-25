package com.metao.book.product.application.config;

import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.infrastructure.factory.handler.MessageHandler;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.shared.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@EnableKafka
@RequiredArgsConstructor
public abstract class KafkaProductListener implements MessageHandler<ProductEvent> {

    private final ProductKafkaHandler productKafkaHandler;
    private final ProductService productService;
    private final ProductMapper productMapper;

    @KafkaListener(id = "product-listener", topics = "product", groupId = "products-grp")
    public void onEvent(ConsumerRecord<String, ProductEvent> record) {
        log.info("Consumed message -> {}", record.value());
        Optional.of(record.value())
            .flatMap(productMapper::toEntity)
            .ifPresent(productService::saveProduct);
    }

}
