package com.metao.book.product.application.config;

import com.metao.book.product.application.service.OrderManagerService;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.Status;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableKafka
@Transactional
@RequiredArgsConstructor
@Profile({"!test"})
@ImportAutoConfiguration(value = KafkaSerdesConfig.class)
public class KafkaListenerComponent {

    private final OrderManagerService orderManagerService;
    private final ProductRepository productRepository;
    private final ProductMapper mapper;
    private final CountDownLatch count = new CountDownLatch(1);

    @KafkaListener(id = "products", topics = "${kafka.topic.product}", groupId = "12")
    public void onProductEvent(ConsumerRecord<String, ProductEvent> productEvent) {
        var product = productEvent.value();
        log.info("Consumed product -> {}", product);
        var foundProduct = productRepository.findByAsin(product.getProductId());
        if (foundProduct.isEmpty()) {
            Optional.of(product)
                .flatMap(mapper::toEntity)
                .ifPresent(productRepository::save);
        }
        count.countDown();
    }


    @KafkaListener(id = "orders", topics = "${kafka.topic.order}")
    public void onOrderEvent(ConsumerRecord<String, OrderEvent> orderRecord) {
        try {
            count.await(5, TimeUnit.MINUTES);
            var order = orderRecord.value();
            log.info("Consumed order -> {}", order);
            if (Status.NEW.equals(order.getStatus())) {
                orderManagerService.reserve(order);
            } else {
                orderManagerService.confirm(order);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
