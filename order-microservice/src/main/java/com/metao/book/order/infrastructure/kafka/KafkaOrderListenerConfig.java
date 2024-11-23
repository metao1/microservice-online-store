package com.metao.book.order.infrastructure.kafka;

import com.google.protobuf.Timestamp;
import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.domain.OrderService;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.product.event.ProductUpdatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaOrderListenerConfig {

    private final KafkaTemplate<String, ProductUpdatedEvent> kafkaOrderProducer;
    private final OrderService orderService;

    @Value("${kafka.topic.product-updated.name}")
    private String orderTopic;

    @RetryableTopic
    @KafkaListener(id = "${kafka.topic.order-created.id}", topics = "${kafka.topic.order-created.name}", groupId = "${kafka.topic.order-created.group-id}", containerFactory = "orderCreatedEventKafkaListenerContainerFactory")
    public void onOrderEvent(ConsumerRecord<String, OrderCreatedEvent> orderRecord) {
        StageProcessor.accept(orderRecord.value())
            .map(KafkaOrderMapper::toEntity)
            .map(orderService::save)
            .acceptExceptionally((entity, ex) -> {
                if (ex != null || entity == null) {
                    log.error("error while consuming order, error: {}", ex == null ? "null" : ex.getMessage());
                } else {
                    log.info("order {} saved.", entity);
                }
            });
    }

    @RetryableTopic
    @KafkaListener(id = "${kafka.topic.order-payment.id}", topics = "${kafka.topic.order-payment.name}", groupId = "${kafka.topic.order-payment.group-id}", containerFactory = "orderPaymentEventKafkaListenerContainerFactory")
    public void onOrderPaymentEvent(ConsumerRecord<String, OrderPaymentEvent> orderRecord) {
        StageProcessor.accept(orderRecord.value()).acceptExceptionally((orderPaymentEvent, ex) -> {
            if (ex != null) {
                log.error("can't make order from event :{}", ex.getMessage());
                return;
            }
            var foundOrder = orderService.getOrderByOrderId(orderPaymentEvent.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderPaymentEvent.getId()));
            OrderStatus orderStatus = OrderStatus.valueOf(String.valueOf(orderPaymentEvent.getStatus()));
            foundOrder.setStatus(orderStatus);
            orderService.save(foundOrder);
            if (orderStatus == OrderStatus.CONFIRMED) {
                log.info("order {} confirmed.", orderPaymentEvent.getId());
                var productUpdatedEvent = ProductUpdatedEvent.newBuilder()
                    .setUpdatedTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                    .setAsin(orderPaymentEvent.getId()).setCurrency(foundOrder.getCurrency().toString())
                    .setPrice(orderPaymentEvent.getPrice()).build();
                kafkaOrderProducer.send(orderTopic, orderPaymentEvent.getCustomerId(), productUpdatedEvent);
            }
        });
    }

}
