package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.application.card.OrderRepository;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderMapper;
import com.metao.book.order.domain.OrderNotFoundException;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaOrderListenerConfig {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @RetryableTopic
    @KafkaListener(id = "${kafka.topic.order-created.id}",
        topics = "${kafka.topic.order-created.name}",
        groupId = "${kafka.topic.order-created.group-id}",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory")
    public void onOrderEvent(ConsumerRecord<String, OrderCreatedEvent> orderRecord) {
        StageProcessor.accept(orderRecord.value())
            .map(orderMapper::toEntity)
            .map(orderRepository::save)
            .acceptExceptionally((entity, ex) -> {
                if (ex != null) {
                    log.error("can't save order {} , message :{}", entity, ex.getMessage());
                } else if (entity != null) {
                    log.info("order {} saved.", entity);
                } else {
                    log.error("can't save null order.");
                }
            });
    }

    @RetryableTopic
    @KafkaListener(id = "${kafka.topic.order-payment.id}",
        topics = "${kafka.topic.order-payment.name}",
        groupId = "${kafka.topic.order-payment.group-id}",
        containerFactory = "orderPaymentEventKafkaListenerContainerFactory")
    public void listenToOrderPayment(ConsumerRecord<String, OrderPaymentEvent> orderRecord) {
        StageProcessor
            .accept(orderRecord.value())
            .acceptExceptionally((orderEntity, ex) -> {
                if (ex != null) {
                    log.error("can't make order from event :{}", ex.getMessage());
                    return;
                }
                var foundOrder = orderRepository.findById(new OrderId(orderEntity.getId()))
                    .orElseThrow(() -> new OrderNotFoundException(orderEntity.getId()));
                OrderStatus orderStatus = OrderStatus.valueOf(String.valueOf(orderEntity.getStatus()));
                foundOrder.setStatus(orderStatus);
                orderRepository.save(foundOrder);
            });
    }

}
