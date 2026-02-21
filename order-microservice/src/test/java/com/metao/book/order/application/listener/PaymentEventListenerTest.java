package com.metao.book.order.application.listener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.service.OrderManagementService;
import com.metao.book.order.infrastructure.persistence.repository.ProcessedPaymentEventRepository;
import com.metao.book.shared.OrderPaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventListener")
class PaymentEventListenerTest {

    @Mock
    private OrderManagementService orderManagementService;

    @Mock
    private OrderAggregate order;

    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;

    private PaymentEventListener paymentEventListener;

    @BeforeEach
    void setUp() {
        paymentEventListener = new PaymentEventListener(orderManagementService, processedPaymentEventRepository);
    }

    @Nested
    @DisplayName("Successful Payment Events")
    class SuccessfulPaymentEvents {

        @Test
        @DisplayName("Should reduce inventory and update order status to PAID")
        void shouldReduceInventoryAndUpdateStatusToPaid() {
            String orderIdValue = "order123";
            OrderId orderId = OrderId.of(orderIdValue);
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderIdValue)
                .setPaymentId("payment-1")
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .build();

            when(processedPaymentEventRepository.markProcessed("payment-1")).thenReturn(true);
            when(orderManagementService.getOrderByIdForUpdate(orderId)).thenReturn(order);
            when(order.getStatus()).thenReturn(OrderStatus.CREATED);

            paymentEventListener.handlePaymentEvent(paymentEvent);

            verify(orderManagementService).updateItemQuantity(orderId);
            verify(orderManagementService).updateOrderStatus(orderId, OrderStatus.PAID.name());
        }

        @Test
        @DisplayName("Should skip duplicate successful payment event when order already PAID")
        void shouldSkipDuplicateSuccessfulPaymentEvent() {
            String orderIdValue = "order123";
            OrderId orderId = OrderId.of(orderIdValue);
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderIdValue)
                .setPaymentId("payment-2")
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .build();

            when(processedPaymentEventRepository.markProcessed("payment-2")).thenReturn(true);
            when(orderManagementService.getOrderByIdForUpdate(orderId)).thenReturn(order);
            when(order.getStatus()).thenReturn(OrderStatus.PAID);

            paymentEventListener.handlePaymentEvent(paymentEvent);

            verify(orderManagementService, never()).updateOrderStatus(orderId, OrderStatus.PAID.name());
        }
    }

    @Nested
    @DisplayName("Failed Payment Events")
    class FailedPaymentEvents {

        @Test
        @DisplayName("Should update order status to PAYMENT_FAILED for failed payment")
        void shouldUpdateOrderStatusToPaymentFailedForFailedPayment() {
            String orderIdValue = "order456";
            OrderId orderId = OrderId.of(orderIdValue);
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderIdValue)
                .setPaymentId("payment-3")
                .setStatus(OrderPaymentEvent.Status.FAILED)
                .build();

            when(processedPaymentEventRepository.markProcessed("payment-3")).thenReturn(true);
            paymentEventListener.handlePaymentEvent(paymentEvent);

            verify(orderManagementService).updateOrderStatus(eq(orderId), eq(OrderStatus.PAYMENT_FAILED.name()));
        }

        @Test
        @DisplayName("Should skip duplicate payment event by idempotency key")
        void shouldSkipDuplicatePaymentEventByIdempotencyKey() {
            String orderIdValue = "order456";
            OrderId orderId = OrderId.of(orderIdValue);
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderIdValue)
                .setPaymentId("payment-duplicate")
                .setStatus(OrderPaymentEvent.Status.FAILED)
                .build();

            when(processedPaymentEventRepository.markProcessed("payment-duplicate")).thenReturn(false);

            paymentEventListener.handlePaymentEvent(paymentEvent);

            verify(orderManagementService, never()).updateOrderStatus(eq(orderId), eq(OrderStatus.PAYMENT_FAILED.name()));
        }
    }
}
