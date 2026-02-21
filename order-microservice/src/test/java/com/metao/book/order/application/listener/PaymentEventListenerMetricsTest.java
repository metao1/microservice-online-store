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
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PaymentEventListenerMetricsTest {

    @MockBean
    OrderManagementService orderManagementService;
    @MockBean
    ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Autowired
    PaymentEventListener listener;
    @Autowired
    MeterRegistry meterRegistry;

    @Test
    void timerIsRecordedWhenListenerRuns() {
        var event = OrderPaymentEvent.newBuilder()
            .setOrderId("order-1")
            .setPaymentId("payment-1")
            .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
            .build();

        when(processedPaymentEventRepository.markProcessed("payment-1")).thenReturn(true);
        OrderAggregate stubOrder = mock(OrderAggregate.class);
        when(stubOrder.getStatus()).thenReturn(OrderStatus.CREATED);
        when(orderManagementService.getOrderByIdForUpdate(OrderId.of("order-1"))).thenReturn(stubOrder);

        listener.handlePaymentEvent(event);

        var timer = meterRegistry.find("order.payment.listener").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThan(0);
    }
}
