package com.metao.book.order.application.service;

import com.metao.book.order.application.port.ConsumedMessagePort;
import com.metao.book.order.application.port.OrderPort;
import com.metao.book.order.application.port.ShoppingCartCommandPort;
import com.metao.book.order.application.usecase.HandleOrderPaymentEventCommand;
import com.metao.book.order.application.usecase.HandleOrderPaymentEventUseCase;
import com.metao.book.order.domain.exception.InvalidPaymentEventException;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.PaymentStatus;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.shared.application.messaging.DomainEventPublisherPort;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleOrderPaymentEventService implements HandleOrderPaymentEventUseCase {

    private static final String CONSUMER = "order.payment";

    private final OrderRepository orderRepository;
    private final OrderPort orderPort;
    private final ConsumedMessagePort consumedMessagePort;
    private final ShoppingCartCommandPort shoppingCartCommandPort;
    private final DomainEventPublisherPort domainEventPublisherPort;

    @Override
    @Transactional
    public void handle(HandleOrderPaymentEventCommand command) {
        String eventId = requireNonBlank(command.eventId(), "Payment event ID");
        OrderId orderId = OrderId.of(
            requireNonBlank(command.orderId(), "Order ID")
        );

        if (!consumedMessagePort.claim(CONSUMER, eventId)) {
            log.debug("Ignoring already-consumed payment event {}", eventId);
            return;
        }

        OrderAggregate order = orderPort.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        switch (command.paymentStatus()) {
            case SUCCESSFUL -> applySuccessfulPayment(order);
            case FAILED -> applyFailedPayment(order);
            case CANCELLED -> applyCancelledPayment(order);
            case PENDING -> throw new InvalidPaymentEventException(
                "Payment PENDING is not an actionable order event"
            );
        }

        orderRepository.save(order);
        publishAndClear(order);

        if (command.paymentStatus() == PaymentStatus.SUCCESSFUL
            && order.getStatus() == OrderStatus.PAID) {
            shoppingCartCommandPort.clearCart(order.getUserId().value());
        }
    }

    private void applySuccessfulPayment(OrderAggregate order) {
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        order.updateStatus(OrderStatus.PAID);
        order.requestInventoryReduction();
    }

    private void applyFailedPayment(OrderAggregate order) {
        if (order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            order.updateStatus(OrderStatus.PAYMENT_FAILED);
        }
    }

    private void applyCancelledPayment(OrderAggregate order) {
        if (order.getStatus() != OrderStatus.CANCELLED) {
            order.updateStatus(OrderStatus.CANCELLED);
        }
    }

    private void publishAndClear(OrderAggregate order) {
        order.getDomainEvents().forEach(domainEventPublisherPort::publish);
        order.clearDomainEvents();
    }

    private String requireNonBlank(String s, String message) {
        return Objects.requireNonNull(s, "%s must not be null".formatted(message));
    }
}