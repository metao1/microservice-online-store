package com.metao.book.payment.listener;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderPaymentEvent; // Corrected to use existing OrderPaymentEvent
import com.metao.book.payment.service.PaymentProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventListener.class);

    @Autowired
    private PaymentProcessingService paymentProcessingService;

    @Autowired
    private KafkaTemplate<String, OrderPaymentEvent> kafkaTemplate; // Corrected to use OrderPaymentEvent

    @Value("${kafka.topic.order-payment.name}")
    private String orderPaymentTopicName;
    
    // This value is used by @KafkaListener, ensure it's correctly resolved
    // @Value("${kafka.topic.order-created.name}")
    // private String orderCreatedTopicName; // Not strictly needed as a field if only used in annotation

    @KafkaListener(topics = "${kafka.topic.order-created.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreatedEvent(OrderCreatedEvent orderEvent) {
        log.info("Received OrderCreatedEvent for order item: {}, product: {}", orderEvent.getId(), orderEvent.getProductId());
        
        try {
            OrderPaymentEvent orderPaymentEvent = paymentProcessingService.processPayment(orderEvent); // Corrected type
            // Key for payment event could be orderId (which is orderEvent.getId()) or paymentId
            kafkaTemplate.send(orderPaymentTopicName, orderPaymentEvent.getOrderId(), orderPaymentEvent);
            log.info("Sent OrderPaymentEvent for order item: {}, status: {}", orderPaymentEvent.getOrderId(), orderPaymentEvent.getStatus());
        } catch (Exception e) {
            log.error("Error processing payment for order item {} or sending OrderPaymentEvent", orderEvent.getId(), e);
            // Implement error handling / dead-letter queue logic here if needed
        }
    }
}
