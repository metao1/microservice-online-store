package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.infrastructure.factory.producer.KafkaProductProducer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "book.kafka.isEnabled", havingValue = "true", matchIfMissing = true)
public class ProductKafkaHandler implements Consumer<ProductCreatedEvent> {

    private final KafkaProductProducer kafkaProductProducer;

    @Override
    public void accept(@NonNull ProductCreatedEvent event) {
        log.info("publish product event at: {}", event.getCreateTime());
        kafkaProductProducer.publish(event);
    }

}
