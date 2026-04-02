package com.metao.book.order.application.usecase;

import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.port.ProcessedPaymentEventPort;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.service.OrderManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleOrderPaymentEventUseCase {

    private final OrderManagementService orderManagementService;
    private final ProcessedPaymentEventPort processedPaymentEventPort;
    private final ShoppingCartService shoppingCartService;

    @Transactional
    public void handle(HandleOrderPaymentEventCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        log.info("Received OrderPaymentEvent for order ID: {}, status: {}", orderId.value(), command.paymentStatus());

        if (!processedPaymentEventPort.markProcessed(command.eventId())) {
            log.info("Payment event {} already processed; skipping.", command.eventId());
            return;
        }

        switch (command.paymentStatus()) {
            case "SUCCESSFUL" -> handleSuccessfulPayment(orderId);
            case "FAILED" -> handleFailedPayment(orderId);
            default -> log.warn("Unhandled payment status {} for order {}.", command.paymentStatus(), orderId.value());
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
        shoppingCartService.clearCart(order.getUserId().value());
        log.info("Order {} inventory reduced and status updated to {}.", orderId.value(), OrderStatus.PAID);
    }
}
