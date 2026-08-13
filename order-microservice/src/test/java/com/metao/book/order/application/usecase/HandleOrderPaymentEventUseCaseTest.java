package com.metao.book.order.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.application.port.ConsumedMessagePort;
import com.metao.book.order.application.port.OrderPort;
import com.metao.book.order.application.port.ShoppingCartCommandPort;
import com.metao.book.order.application.service.HandleOrderPaymentEventService;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.shared.application.messaging.DomainEventPublisherPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleOrderPaymentEventUseCaseTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderPort orderPort;
    @Mock
    private ConsumedMessagePort consumedMessagePort;
    @Mock
    private ShoppingCartCommandPort shoppingCartCommandPort;
    @Mock
    private DomainEventPublisherPort domainEventPublisherPort;
    @Mock
    private OrderAggregate order;

    private HandleOrderPaymentEventUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HandleOrderPaymentEventService(
            orderRepository,
            orderPort,
            consumedMessagePort,
            shoppingCartCommandPort,
            domainEventPublisherPort
        );
    }

    @Test
    void successfulPaymentMarksOrderPaidRequestsInventoryAndClearsCart() {
        OrderId orderId = OrderId.of("order-123");
        HandleOrderPaymentEventCommand command = new HandleOrderPaymentEventCommand(
            "payment-event-1",
            orderId.value(),
            "SUCCESSFUL"
        );
        when(consumedMessagePort.claim("order.payment", "payment-event-1")).thenReturn(true);
        when(orderPort.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(order.getStatus()).thenReturn(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
        when(order.getUserId()).thenReturn(UserId.of("user-1"));
        when(order.getDomainEvents()).thenReturn(List.of());

        useCase.handle(command);

        verify(order).updateStatus(OrderStatus.PAID);
        verify(order).requestInventoryReduction();
        verify(orderRepository).save(order);
        verify(shoppingCartCommandPort).clearCart("user-1");
    }

    @Test
    void failedPaymentMarksTheOrderWithoutClearingTheCart() {
        OrderId orderId = OrderId.of("order-456");
        HandleOrderPaymentEventCommand command = new HandleOrderPaymentEventCommand(
            "payment-event-2",
            orderId.value(),
            "FAILED"
        );
        when(consumedMessagePort.claim("order.payment", "payment-event-2")).thenReturn(true);
        when(orderPort.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(order.getStatus()).thenReturn(OrderStatus.PENDING_PAYMENT);
        when(order.getDomainEvents()).thenReturn(List.of());

        useCase.handle(command);

        verify(order).updateStatus(OrderStatus.PAYMENT_FAILED);
        verify(orderRepository).save(order);
        verify(shoppingCartCommandPort, never()).clearCart(anyString());
    }

    @Test
    void duplicateEventDoesNotLoadOrChangeTheOrder() {
        HandleOrderPaymentEventCommand command = new HandleOrderPaymentEventCommand(
            "payment-event-duplicate",
            "order-789",
            "SUCCESSFUL"
        );
        when(consumedMessagePort.claim("order.payment", "payment-event-duplicate")).thenReturn(false);

        useCase.handle(command);

        verify(orderPort, never()).findByIdForUpdate(any());
        verify(orderRepository, never()).save(any());
        verify(shoppingCartCommandPort, never()).clearCart(anyString());
    }
}
