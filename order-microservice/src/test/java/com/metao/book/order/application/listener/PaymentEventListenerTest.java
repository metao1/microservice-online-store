package com.metao.book.order.application.listener;

import com.metao.book.order.application.service.OrderApplicationService;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.shared.OrderPaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentEventListener
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventListener")
class PaymentEventListenerTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    private PaymentEventListener paymentEventListener;

    @BeforeEach
    void setUp() {
        paymentEventListener = new PaymentEventListener(orderApplicationService);
    }

    @Nested
    @DisplayName("Successful Payment Events")
    class SuccessfulPaymentEvents {

        @Test
        @DisplayName("Should update order status to PAID for successful payment")
        void shouldUpdateOrderStatusToPaidForSuccessfulPayment() {
            // Given
            String orderId = "order123";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setCustomerId("customer123")
                .setProductId("product123")
                .build();

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAID.name())
            );
        }

        @Test
        @DisplayName("Should handle successful payment with different products")
        void shouldHandleSuccessfulPaymentWithDifferentProducts() {
            // Given
            String orderId = "order456";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setCustomerId("customer456")
                .setProductId("product789")
                .build();

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAID.name())
            );
        }
    }

    @Nested
    @DisplayName("Failed Payment Events")
    class FailedPaymentEvents {

        @Test
        @DisplayName("Should update order status to PAYMENT_FAILED for failed payment")
        void shouldUpdateOrderStatusToPaymentFailedForFailedPayment() {
            // Given
            String orderId = "order456";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.FAILED)
                .setCustomerId("customer456")
                .setProductId("product456")
                .build();

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAYMENT_FAILED.name())
            );
        }

        @Test
        @DisplayName("Should handle failed payment with error message")
        void shouldHandleFailedPaymentWithErrorMessage() {
            // Given
            String orderId = "order789";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.FAILED)
                .setCustomerId("customer789")
                .setProductId("product789")
                .setErrorMessage("Insufficient funds")
                .build();

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAYMENT_FAILED.name())
            );
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle order not found exception gracefully")
        void shouldHandleOrderNotFoundExceptionGracefully() {
            // Given
            String orderId = "nonexistent-order";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setCustomerId("customer123")
                .setProductId("product123")
                .build();

            doThrow(new RuntimeException("Order not found"))
                .when(orderApplicationService)
                .updateOrderStatus(any(), any());

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAID.name())
            );
            // Should not re-throw the exception
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() {
            // Given
            String orderId = "order123";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.FAILED)
                .setCustomerId("customer123")
                .setProductId("product123")
                .build();

            doThrow(new RuntimeException("Database connection error"))
                .when(orderApplicationService)
                .updateOrderStatus(any(), any());

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAYMENT_FAILED.name())
            );
            // Should not re-throw the exception
        }
    }

    @Nested
    @DisplayName("Event Data Validation")
    class EventDataValidation {

        @Test
        @DisplayName("Should handle empty order ID")
        void shouldHandleEmptyOrderId() {
            // Given
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId("") // Empty order ID
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setCustomerId("customer123")
                .setProductId("product123")
                .build();

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of("")),
                eq(OrderStatus.PAID.name())
            );
        }

        @Test
        @DisplayName("Should handle missing customer ID")
        void shouldHandleMissingCustomerId() {
            // Given
            String orderId = "order123";
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderId)
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setProductId("product123")
                // No customer ID set
                .build();

            // When
            paymentEventListener.handlePaymentEvent(paymentEvent);

            // Then
            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(orderId)),
                eq(OrderStatus.PAID.name())
            );
        }
    }
}
