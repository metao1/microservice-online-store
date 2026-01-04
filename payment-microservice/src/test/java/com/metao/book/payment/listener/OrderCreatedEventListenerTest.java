package com.metao.book.payment.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.Message;
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
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private PaymentProcessingService paymentProcessingService;

    @Mock
    private KafkaEventHandler eventHandler;

    @Mock
    private KafkaTemplate<String, Message> kafkaTemplate;

    @InjectMocks
    private OrderCreatedEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new OrderCreatedEventListener(paymentProcessingService, eventHandler, kafkaTemplate);
    }

    @Test
    void handleOrderCreatedEvent_processesPaymentAndSendsEvent() {
        // Given
        String topic = "order-payment-topic";
        OrderCreatedEvent orderEvent = OrderCreatedEvent.newBuilder().setId("orderItem123").build();
        OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
            .setOrderId("orderItem123")
            .setStatus(OrderPaymentEvent.Status.SUCCESSFUL).build();

        // When
        when(paymentProcessingService.processPayment(any(OrderCreatedEvent.class))).thenReturn(paymentEvent);

        when(eventHandler.getKafkaTopic(any(Class.class))).thenReturn(topic);

        eventListener.handleOrderCreatedEvent(orderEvent);

        // Then
        verify(paymentProcessingService).processPayment(orderEvent);
        // Verify that eventHandler.handle is called with the correct key and payload
        verify(kafkaTemplate).send(eq(topic), eq(paymentEvent.getOrderId()), eq(paymentEvent));
    }
}

