package com.metao.book.order.application.listener;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.service.OrderManagementService;
import com.metao.book.shared.OrderPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderManagementService orderManagementService;

    @Transactional
    @KafkaListener(
        id = "${kafka.topic.order-payment.id}",
        topics = "${kafka.topic.order-payment.name}",
        groupId = "${kafka.topic.order-payment.group-id}",
        containerFactory = "orderPaymentEventKafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(OrderPaymentEvent paymentEvent) {
        log.info("Received OrderPaymentEvent for order ID: {}, status: {}",
            paymentEvent.getOrderId(), paymentEvent.getStatus());

        try {
            final String newStatus = switch (paymentEvent.getStatus()) {
                case SUCCESSFUL -> {
                    log.info("Order {} status will be updated to PAID.", paymentEvent.getOrderId());
                    yield OrderStatus.PAID.name();
                }
                case FAILED -> {
                    log.info("Order {} status will be updated to PAYMENT_FAILED.", paymentEvent.getOrderId());
                    yield OrderStatus.PAYMENT_FAILED.name();
                }
                default -> {
                    log.warn("Unhandled payment status {} for order {}.",
                        paymentEvent.getStatus(), paymentEvent.getOrderId());
                    yield null;
                }
            };

            orderManagementService.updateOrderStatus(OrderId.of(paymentEvent.getOrderId()), newStatus);
            log.info("Order {} status successfully updated to {}.", paymentEvent.getOrderId(), newStatus);

        } catch (RuntimeException e) {
            log.warn("Order {} not found for payment event processing: {}", paymentEvent.getOrderId(), e.getMessage());
        }
    }
}
