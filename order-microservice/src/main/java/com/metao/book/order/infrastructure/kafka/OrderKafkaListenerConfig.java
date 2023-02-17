package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.application.config.KafkaSerdesConfig;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.application.service.OrderValidator;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.OrderEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
@ImportAutoConfiguration(value = {KafkaSerdesConfig.class})
public class OrderKafkaListenerConfig {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderValidator orderValidator;

    @Transactional(value = "kafkaTransactionManager")
    @KafkaListener(id = "order-listener-id", topics = "${kafka.topic.order}", groupId = "order-listener-group")
    public void orderKafkaListener(ConsumerRecord<String, OrderEvent> record) {
        var order = record.value();
        orderValidator.validate(order);
        log.info("Consumed order -> {}", order);
        final var orderId = new OrderId(order.getOrderId());
        final var orderEntity = orderMapper.toEntity(order);
        new OptionalHelper<>(orderRepository.getReferenceById(orderId))
            .ifPresentRun(() -> orderEntity
                , (exception) -> {
                    if (exception instanceof EntityNotFoundException) {
                        log.debug("entity {} not found.", orderEntity);
                        orderRepository.save(orderEntity);
                    }
                }
                , (updatedOrder) -> {
                    orderRepository.save(orderEntity);
                    log.debug("old order {} has been updated.", orderEntity);
                });
    }

}
