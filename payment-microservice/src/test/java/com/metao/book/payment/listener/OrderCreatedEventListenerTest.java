package com.metao.book.payment.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.service.PaymentProcessingService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.kafka.KafkaEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private PaymentProcessingService paymentProcessingService;

    @Mock
    private KafkaEventHandler eventHandler;

    @InjectMocks
    private OrderCreatedEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new OrderCreatedEventListener(paymentProcessingService, eventHandler);
    }

    @Test
    void handleOrderCreatedEvent_processesPaymentAndSendsEvent() {
        OrderCreatedEvent orderEvent = OrderCreatedEvent.newBuilder().setId("orderItem123").build();
        OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                                            .setOrderId("orderItem123") 
                                            .setStatus(OrderPaymentEvent.Status.SUCCESSFUL).build();

        when(paymentProcessingService.processPayment(any(OrderCreatedEvent.class))).thenReturn(paymentEvent);

        eventListener.handleOrderCreatedEvent(orderEvent);

        verify(paymentProcessingService).processPayment(orderEvent);
        // Verify that eventHandler.handle is called with the correct key and payload
        verify(eventHandler).handle(paymentEvent.getOrderId(), paymentEvent);
    }
}

