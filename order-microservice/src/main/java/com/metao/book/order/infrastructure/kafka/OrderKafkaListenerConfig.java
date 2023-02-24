package com.metao.book.order.infrastructure.kafka;

import static com.metao.book.shared.kafka.Constants.KAFKA_TRANSACTION_MANAGER;

import com.metao.book.order.application.config.KafkaSerdesConfig;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.application.service.OrderValidator;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.OrderEvent;
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

    @KafkaListener(id = "${kafka.topic.order}",
        topics = "${kafka.topic.order}",
        groupId = "${kafka.topic.order}" + "-grp2")
    @Transactional(KAFKA_TRANSACTION_MANAGER)
    public void orderKafkaListener(ConsumerRecord<String, OrderEvent> record) {
        var order = record.value();
        orderValidator.validate(order);
        log.info("Consumed order -> {}", order);
        new OrderProcessorHelper<>(order)
            .ifPresentRun((orderEvent) -> {
                    final var orderEntity = orderMapper.toEntity(orderEvent);
                    return orderRepository.getReferenceById(orderEntity.id());
                },
                (orderEvent, obj) -> {
                    final var orderEntity = orderMapper.toEntity(orderEvent);
                    orderRepository.save(orderEntity);
                    log.info("saved order -> {}", orderEntity);
                },
                (exception) -> log.debug("Could not processed event.", exception));
    }

}
