package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.event.ProductCreatedEvent;
import com.metao.book.product.infrastructure.factory.producer.KafkaProductProducer;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductKafkaHandler implements MessageHandler<ProductCreatedEvent> {

    private final KafkaProductProducer kafkaProductProducer;

    @Override
    public void onMessage(@NonNull ProductCreatedEvent event) {
        log.info("sending product to kafka on timestamp: {}", event.occurredOn());
        StageProcessor.accept(event)
            .acceptExceptionally((e, exp) -> {
                kafkaProductProducer.sendToKafka(e);
                log.error("error sending product {}", exp.getMessage());
            });
    }

}
