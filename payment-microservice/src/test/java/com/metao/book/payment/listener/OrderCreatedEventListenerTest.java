package com.metao.book.payment.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.service.PaymentProcessingService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private PaymentProcessingService paymentProcessingService;

    @Mock
    private KafkaTemplate<String, OrderPaymentEvent> kafkaTemplate;

    @InjectMocks
    private OrderCreatedEventListener eventListener;

    private final String testPaymentTopic = "test-order-payment-events";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventListener, "orderPaymentTopicName", testPaymentTopic);
    }

    @Test
    void handleOrderCreatedEvent_processesPaymentAndSendsEvent() {
        OrderCreatedEvent orderEvent = OrderCreatedEvent.newBuilder().setId("orderItem123").build();
        OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                                            .setOrderId("orderItem123") 
                                            .setStatus(OrderPaymentEvent.Status.SUCCESSFUL).build();

        when(paymentProcessingService.processPayment(any(OrderCreatedEvent.class))).thenReturn(paymentEvent);

        eventListener.handleOrderCreatedEvent(orderEvent);

        verify(paymentProcessingService).processPayment(eq(orderEvent));
        // Verify that KafkaTemplate.send is called with the correct topic, key (orderId from paymentEvent), and payload
        verify(kafkaTemplate).send(eq(testPaymentTopic), eq(paymentEvent.getOrderId()), eq(paymentEvent));
    }
}

