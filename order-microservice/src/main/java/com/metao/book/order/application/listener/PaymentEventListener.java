package com.metao.book.order.application.listener;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.service.OrderManagementService;
import com.metao.book.order.infrastructure.persistence.repository.ProcessedPaymentEventRepository;
import com.metao.book.shared.OrderPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.annotation.Timed;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderManagementService orderManagementService;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Transactional
    @KafkaListener(
        id = "${kafka.topic.order-payment.id}",
        topics = "${kafka.topic.order-payment.name}",
        groupId = "${kafka.topic.order-payment.group-id}",
        containerFactory = "orderPaymentEventKafkaListenerContainerFactory"
    )
    @Timed(value = "order.payment.listener", extraTags = {"listener", "order-payment"})
    public void handlePaymentEvent(OrderPaymentEvent paymentEvent) {
        OrderId orderId = OrderId.of(paymentEvent.getOrderId());
        String eventId = resolveEventId(paymentEvent);
        log.info("Received OrderPaymentEvent for order ID: {}, status: {}", orderId.value(), paymentEvent.getStatus());

        if (!processedPaymentEventRepository.markProcessed(eventId)) {
            log.info("Payment event {} already processed; skipping.", eventId);
            return;
        }

        switch (paymentEvent.getStatus()) {
            case SUCCESSFUL -> handleSuccessfulPayment(orderId);
            case FAILED -> handleFailedPayment(orderId);
            default -> log.warn("Unhandled payment status {} for order {}.", paymentEvent.getStatus(), orderId.value());
        }
    }

    private void handleFailedPayment(OrderId orderId) {
        log.info("Order {} status will be updated to PAYMENT_FAILED.", orderId.value());
        orderManagementService.updateOrderStatus(orderId, OrderStatus.PAYMENT_FAILED.name());
        log.info("Order {} status successfully updated to {}.", orderId.value(), OrderStatus.PAYMENT_FAILED);
    }

    private void handleSuccessfulPayment(OrderId orderId) {
        OrderAggregate order = orderManagementService.getOrderByIdForUpdate(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Order {} already PAID; skipping duplicate successful payment event.", orderId.value());
            return;
        }

        log.info("Reducing inventory for order {} items before marking order as PAID.", orderId.value());
        orderManagementService.updateItemQuantity(orderId);
        orderManagementService.updateOrderStatus(orderId, OrderStatus.PAID.name());
        log.info("Order {} inventory reduced and status updated to {}.", orderId.value(), OrderStatus.PAID);
    }

    private String resolveEventId(OrderPaymentEvent paymentEvent) {
        if (!paymentEvent.getPaymentId().isBlank()) {
            return paymentEvent.getPaymentId();
        }
        long seconds = paymentEvent.hasCreateTime() ? paymentEvent.getCreateTime().getSeconds() : 0L;
        int nanos = paymentEvent.hasCreateTime() ? paymentEvent.getCreateTime().getNanos() : 0;
        return paymentEvent.getOrderId() + ":" + paymentEvent.getStatus().name() + ":" + seconds + ":" + nanos;
    }
}
