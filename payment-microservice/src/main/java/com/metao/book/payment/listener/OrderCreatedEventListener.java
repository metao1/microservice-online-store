package com.metao.book.payment.listener;

import com.google.protobuf.Message;
import com.metao.book.payment.infrastructure.persistence.repository.ProcessedOrderCreatedEventRepository;
import com.metao.book.payment.service.PaymentProcessingService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.kafka.KafkaEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final PaymentProcessingService paymentProcessingService;
    private final ProcessedOrderCreatedEventRepository processedOrderCreatedEventRepository;
    private final KafkaEventHandler kafkaEventHandler;
    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Transactional
    @KafkaListener(
        id = "${kafka.topic.order-created.id}",
        topics = "${kafka.topic.order-created.name}",
        groupId = "${kafka.topic.order-created.group-id}",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(OrderCreatedEvent orderEvent) {
        log.info("Received OrderCreatedEvent for order item: {}, product: {}", orderEvent.getId(),
            orderEvent.getSku());
        String eventId = orderEvent.getId();
        if (!processedOrderCreatedEventRepository.markProcessed(eventId)) {
            log.info("OrderCreatedEvent {} already processed; skipping duplicate.", eventId);
            return;
        }

        OrderPaymentEvent orderPaymentEvent = paymentProcessingService.processPayment(orderEvent);
        var kafkaTopic = kafkaEventHandler.getKafkaTopic(orderPaymentEvent.getClass());
        kafkaTemplate.send(kafkaTopic, orderPaymentEvent.getOrderId(), orderPaymentEvent);
        log.info("Sent OrderPaymentEvent for order item: {}, status: {}", orderPaymentEvent.getOrderId(),
            orderPaymentEvent.getStatus());
    }
}
