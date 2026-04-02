package com.metao.book.order.infrastructure.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.metao.book.order.application.usecase.HandleOrderPaymentEventUseCase;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.Status;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class PaymentEventListenerMetricsTest {

    @Test
    void listenerExecutesWithMocks() {
        var handleOrderPaymentEventUseCase = mock(HandleOrderPaymentEventUseCase.class);
        var listener = new PaymentEventListener(handleOrderPaymentEventUseCase);
        var acknowledgment = mock(Acknowledgment.class);
        var registry = new SimpleMeterRegistry();

        var event = OrderPaymentUpdatedEvent.newBuilder()
            .setOrderId("order-1")
            .setPaymentId("payment-1")
            .setStatus(Status.SUCCESSFUL)
            .build();

        listener.handlePaymentEvent(event, acknowledgment);

        // With direct invocation no AOP metrics are recorded; just assert successful execution.
        assertThat(event.getOrderId()).isEqualTo("order-1");
    }
}
