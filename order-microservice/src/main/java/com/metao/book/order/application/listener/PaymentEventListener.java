package com.metao.book.order.application.listener;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderService;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.shared.OrderPaymentEvent;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private OrderService orderService;

    @Value("${kafka.topic.order-payment.name}")
    private String orderPaymentTopic; 

    @Transactional
    @KafkaListener(topics = "${kafka.topic.order-payment.name}", 
                   groupId = "order-service-payment-group", 
                   containerFactory = "orderPaymentEventKafkaListenerContainerFactory")
    public void handlePaymentEvent(OrderPaymentEvent paymentEvent) {
        log.info("Received OrderPaymentEvent for order item ID: {}, status: {}", 
                 paymentEvent.getOrderId(), paymentEvent.getStatus());

        Optional<OrderEntity> orderOptional = orderService.getOrderByOrderId(paymentEvent.getOrderId());

        if (orderOptional.isEmpty()) {
            log.warn("Order item with ID {} not found, cannot process payment event.", paymentEvent.getOrderId());
            return;
        }

        OrderEntity orderEntity = orderOptional.get();
        
        // Prevent processing if order is already in a terminal state from payment perspective
        if (orderEntity.getStatus() == OrderStatus.PAID || 
            orderEntity.getStatus() == OrderStatus.PAYMENT_FAILED) {
            log.warn("Order item {} already in a final payment status ({}). Ignoring payment event.", 
                     orderEntity.getOrderId(), orderEntity.getStatus());
            return;
        }

        switch (paymentEvent.getStatus()) {
            case SUCCESSFUL:
                orderEntity.setStatus(OrderStatus.PAID); 
                log.info("Order item {} status updated to PAID.", orderEntity.getOrderId());
                break;
            case FAILED:
                orderEntity.setStatus(OrderStatus.PAYMENT_FAILED); 
                log.info("Order item {} status updated to PAYMENT_FAILED. Status: {}", orderEntity.getOrderId(), paymentEvent.getStatus());
                break;
            default:
                log.warn("Unhandled payment status {} for order item {}.", 
                         paymentEvent.getStatus(), orderEntity.getOrderId());
                return; 
        }
        orderService.save(orderEntity);
    }
}
