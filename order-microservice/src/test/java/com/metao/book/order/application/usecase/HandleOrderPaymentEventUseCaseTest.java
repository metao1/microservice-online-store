package com.metao.book.order.application.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.port.ProcessedPaymentEventPort;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.service.OrderManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HandleOrderPaymentEventUseCase")
class HandleOrderPaymentEventUseCaseTest {

    @Mock
    private OrderManagementService orderManagementService;

    @Mock
    private OrderAggregate order;

    @Mock
    private ProcessedPaymentEventPort processedPaymentEventPort;

    @Mock
    private ShoppingCartService shoppingCartService;

    @InjectMocks
    private HandleOrderPaymentEventUseCase useCase;

    @Nested
    @DisplayName("Successful Payment Events")
    class SuccessfulPaymentEvents {

        @Test
        @DisplayName("Should reduce inventory and update order status to PAID")
        void shouldReduceInventoryAndUpdateStatusToPaid() {
            String orderIdValue = "order123";
            OrderId orderId = OrderId.of(orderIdValue);
            HandleOrderPaymentEventCommand command =
                new HandleOrderPaymentEventCommand("payment-1", orderIdValue, "SUCCESSFUL");

            when(processedPaymentEventPort.markProcessed("payment-1")).thenReturn(true);
            when(orderManagementService.getOrderByIdForUpdate(orderId)).thenReturn(order);
            when(order.getStatus()).thenReturn(OrderStatus.CREATED);
            when(order.getUserId()).thenReturn(UserId.of("user-1"));

            useCase.handle(command);

            verify(orderManagementService).updateItemQuantity(orderId);
            verify(orderManagementService).updateOrderStatus(orderId, OrderStatus.PAID.name());
            verify(shoppingCartService).clearCart("user-1");
        }

        @Test
        @DisplayName("Should skip duplicate successful payment event when order already PAID")
        void shouldSkipDuplicateSuccessfulPaymentEvent() {
            String orderIdValue = "order123";
            OrderId orderId = OrderId.of(orderIdValue);
            HandleOrderPaymentEventCommand command =
                new HandleOrderPaymentEventCommand("payment-2", orderIdValue, "SUCCESSFUL");

            when(processedPaymentEventPort.markProcessed("payment-2")).thenReturn(true);
            when(orderManagementService.getOrderByIdForUpdate(orderId)).thenReturn(order);
            when(order.getStatus()).thenReturn(OrderStatus.PAID);

            useCase.handle(command);

            verify(orderManagementService, never()).updateOrderStatus(orderId, OrderStatus.PAID.name());
            verify(shoppingCartService, never()).clearCart(anyString());
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
            HandleOrderPaymentEventCommand command =
                new HandleOrderPaymentEventCommand("payment-3", orderIdValue, "FAILED");

            when(processedPaymentEventPort.markProcessed("payment-3")).thenReturn(true);

            useCase.handle(command);

            verify(orderManagementService).updateOrderStatus(eq(orderId), eq(OrderStatus.PAYMENT_FAILED.name()));
            verify(shoppingCartService, never()).clearCart(anyString());
        }

        @Test
        @DisplayName("Should skip duplicate payment event by idempotency key")
        void shouldSkipDuplicatePaymentEventByIdempotencyKey() {
            String orderIdValue = "order456";
            OrderId orderId = OrderId.of(orderIdValue);
            HandleOrderPaymentEventCommand command =
                new HandleOrderPaymentEventCommand("payment-duplicate", orderIdValue, "FAILED");

            when(processedPaymentEventPort.markProcessed("payment-duplicate")).thenReturn(false);

            useCase.handle(command);

            verify(orderManagementService, never()).updateOrderStatus(eq(orderId), eq(OrderStatus.PAYMENT_FAILED.name()));
            verify(shoppingCartService, never()).clearCart(anyString());
        }
    }
}
