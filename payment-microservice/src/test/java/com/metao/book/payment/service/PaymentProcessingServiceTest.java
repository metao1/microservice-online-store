package com.metao.book.payment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentProcessingServiceTest {

    private PaymentProcessingService paymentProcessingService;
    
    // Note: Testing the exact random outcome (SUCCESSFUL/FAILED) is tricky without refactoring
    // PaymentProcessingService to inject Random or use a different strategy.
    // This test verifies that a valid OrderPaymentEvent is returned with one of the possible outcomes.

    @BeforeEach
    void setUp() {
        paymentProcessingService = new PaymentProcessingService();
    }

    private OrderCreatedEvent createSampleOrderCreatedEvent() {
        return OrderCreatedEvent.newBuilder()
                .setId("orderItem123")
                .setProductId("prod789")
                .setCustomerId("cust456")
                .setPrice(100.00)
                .setQuantity(1.0)
                .setCurrency("USD")
                .setStatus(OrderCreatedEvent.Status.NEW)
                .build();
    }

    @Test
    void processPayment_returnsPaymentStatusEvent() {
        OrderCreatedEvent orderEvent = createSampleOrderCreatedEvent();
        OrderPaymentEvent paymentEvent = paymentProcessingService.processPayment(orderEvent);

        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getOrderId()).isEqualTo(orderEvent.getId());
        assertThat(paymentEvent.getPaymentId()).isNotNull().isNotEmpty();
        assertThat(paymentEvent.getStatus()).isIn(OrderPaymentEvent.Status.SUCCESSFUL, OrderPaymentEvent.Status.FAILED);
        assertThat(paymentEvent.getCreateTime()).isNotNull();
        
        if (paymentEvent.getStatus() == OrderPaymentEvent.Status.SUCCESSFUL) {
            assertThat(paymentEvent.getErrorMessage()).isEmpty(); 
        } else { // FAILED
            assertThat(paymentEvent.getErrorMessage()).isEqualTo("Payment failed due to insufficient funds (mock).");
        }
    }
}

