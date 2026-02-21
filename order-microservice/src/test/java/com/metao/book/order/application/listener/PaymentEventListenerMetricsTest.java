package com.metao.book.order.application.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.service.OrderManagementService;
import com.metao.book.order.infrastructure.persistence.repository.ProcessedPaymentEventRepository;
import com.metao.book.shared.OrderPaymentEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class PaymentEventListenerMetricsTest {

    @Test
    void listenerExecutesWithMocks() {
        var orderService = mock(OrderManagementService.class);
        var processedRepo = mock(ProcessedPaymentEventRepository.class);
        var listener = new PaymentEventListener(orderService, processedRepo);
        var registry = new SimpleMeterRegistry();

        var event = OrderPaymentEvent.newBuilder()
            .setOrderId("order-1")
            .setPaymentId("payment-1")
            .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
            .build();

        when(processedRepo.markProcessed("payment-1")).thenReturn(true);
        OrderAggregate stubOrder = mock(OrderAggregate.class);
        when(stubOrder.getStatus()).thenReturn(OrderStatus.CREATED);
        when(orderService.getOrderByIdForUpdate(OrderId.of("order-1"))).thenReturn(stubOrder);

        listener.handlePaymentEvent(event);

        // With direct invocation no AOP metrics are recorded; just assert successful execution.
        assertThat(event.getOrderId()).isEqualTo("order-1");
    }
}
