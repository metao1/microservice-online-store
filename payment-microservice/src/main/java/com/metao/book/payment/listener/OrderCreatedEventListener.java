package com.metao.book.payment.listener;

import com.metao.book.payment.service.PaymentProcessingService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.kafka.KafkaEventHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Import(KafkaEventHandler.class)
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventListener.class);

    private final PaymentProcessingService paymentProcessingService;

    private final KafkaEventHandler eventHandler;

    @KafkaListener(topics = "${kafka.topic.order-created.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreatedEvent(OrderCreatedEvent orderEvent) {
        log.info("Received OrderCreatedEvent for order item: {}, product: {}", orderEvent.getId(), orderEvent.getProductId());
        
        try {
            OrderPaymentEvent orderPaymentEvent = paymentProcessingService.processPayment(orderEvent);
            // Key for payment event could be orderId (which is orderEvent.getId()) or paymentId

            eventHandler.handle(orderEvent.getId(), orderPaymentEvent);
            log.info("Sent OrderPaymentEvent for order item: {}, status: {}", orderPaymentEvent.getOrderId(), orderPaymentEvent.getStatus());
        } catch (Exception e) {
            log.error("Error processing payment for order item {} or sending OrderPaymentEvent", orderEvent.getId(), e);
            // Implement error handling / dead-letter queue logic here if needed
        }
    }
}
