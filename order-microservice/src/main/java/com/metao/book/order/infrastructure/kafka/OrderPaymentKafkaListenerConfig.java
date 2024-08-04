package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.application.dto.exception.OrderNotFoundException;
import com.metao.book.order.application.service.OrderPaymentFactory;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.application.service.StageProcessor;
import com.metao.book.shared.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentKafkaListenerConfig {

    private final OrderRepository orderRepository;

    private final OrderPaymentFactory orderMapperFactory;

    @KafkaListener(id = "${kafka.topic.order-payment.id}",
        topics = "${kafka.topic.order-payment.name}",
        groupId = "${kafka.topic.order-payment.group-id}")
    public void listenToOrderPayment(ConsumerRecord<String, OrderPaymentEvent> orderRecord) {
        StageProcessor
            .accept(orderRecord.value())
            .acceptExceptionally((orderEntity, ex) -> {
                if (ex != null) {
                    log.error("can't make order from event :{}", ex.getMessage());
                    return;
                }
                var foundOrder = orderRepository.findByOrderId(new OrderId(orderEntity.getId()))
                    .orElseThrow(OrderNotFoundException::new);
                OrderStatus orderStatus = OrderStatus.valueOf(String.valueOf(orderEntity.getStatus()));
                foundOrder.setStatus(orderStatus);
                orderRepository.save(foundOrder);
            });
    }
}
