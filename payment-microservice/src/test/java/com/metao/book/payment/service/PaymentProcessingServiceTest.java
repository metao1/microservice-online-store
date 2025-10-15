package com.metao.book.payment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.service.PaymentApplicationService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

    @Mock
    private PaymentApplicationService paymentApplicationService;

    private PaymentProcessingService paymentProcessingService;
    
    // Note: Testing the exact random outcome (SUCCESSFUL/FAILED) is tricky without refactoring
    // PaymentProcessingService to inject Random or use a different strategy.
    // This test verifies that a valid OrderPaymentEvent is returned with one of the possible outcomes.

    @BeforeEach
    void setUp() {
        paymentProcessingService = new PaymentProcessingService(paymentApplicationService);
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
    void processPayment_returnsSuccessfulPaymentEvent() {
        // Given
        OrderCreatedEvent orderEvent = createSampleOrderCreatedEvent();
        PaymentDTO successfulPayment = PaymentDTO.builder()
            .paymentId("payment-123")
            .orderId(orderEvent.getId())
            .amount(BigDecimal.valueOf(orderEvent.getPrice()))
            .currency(Currency.getInstance(orderEvent.getCurrency()))
            .status("SUCCESSFUL")
            .isSuccessful(true)
            .isCompleted(true)
            .createdAt(Instant.now())
            .build();

        when(paymentApplicationService.processOrderCreatedEvent(
            orderEvent.getId(),
            BigDecimal.valueOf(orderEvent.getPrice()),
            orderEvent.getCurrency()
        )).thenReturn(successfulPayment);

        // When
        OrderPaymentEvent paymentEvent = paymentProcessingService.processPayment(orderEvent);

        // Then
        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getOrderId()).isEqualTo(orderEvent.getId());
        assertThat(paymentEvent.getPaymentId()).isEqualTo("payment-123");
        assertThat(paymentEvent.getStatus()).isEqualTo(OrderPaymentEvent.Status.SUCCESSFUL);
        assertThat(paymentEvent.getCreateTime()).isNotNull();
        assertThat(paymentEvent.getErrorMessage()).isEmpty();

        verify(paymentApplicationService).processOrderCreatedEvent(
            orderEvent.getId(),
            BigDecimal.valueOf(orderEvent.getPrice()),
            orderEvent.getCurrency()
        );
    }

    @Test
    void processPayment_returnsFailedPaymentEvent() {
        // Given
        OrderCreatedEvent orderEvent = createSampleOrderCreatedEvent();
        PaymentDTO failedPayment = PaymentDTO.builder()
            .paymentId("payment-456")
            .orderId(orderEvent.getId())
            .amount(BigDecimal.valueOf(orderEvent.getPrice()))
            .currency(Currency.getInstance(orderEvent.getCurrency()))
            .status("FAILED")
            .failureReason("Insufficient funds")
            .isSuccessful(false)
            .isCompleted(true)
            .createdAt(Instant.now())
            .build();

        when(paymentApplicationService.processOrderCreatedEvent(
            orderEvent.getId(),
            BigDecimal.valueOf(orderEvent.getPrice()),
            orderEvent.getCurrency()
        )).thenReturn(failedPayment);

        // When
        OrderPaymentEvent paymentEvent = paymentProcessingService.processPayment(orderEvent);

        // Then
        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getOrderId()).isEqualTo(orderEvent.getId());
        assertThat(paymentEvent.getPaymentId()).isEqualTo("payment-456");
        assertThat(paymentEvent.getStatus()).isEqualTo(OrderPaymentEvent.Status.FAILED);
        assertThat(paymentEvent.getCreateTime()).isNotNull();
        assertThat(paymentEvent.getErrorMessage()).isEqualTo("Insufficient funds");
    }

    @Test
    void processPayment_handlesException() {
        // Given
        OrderCreatedEvent orderEvent = createSampleOrderCreatedEvent();

        when(paymentApplicationService.processOrderCreatedEvent(
            any(), any(), any()
        )).thenThrow(new RuntimeException("Payment service error"));

        // When
        OrderPaymentEvent paymentEvent = paymentProcessingService.processPayment(orderEvent);

        // Then
        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getOrderId()).isEqualTo(orderEvent.getId());
        assertThat(paymentEvent.getPaymentId()).startsWith("FAILED-");
        assertThat(paymentEvent.getStatus()).isEqualTo(OrderPaymentEvent.Status.FAILED);
        assertThat(paymentEvent.getErrorMessage()).contains("Payment processing failed");
    }
}

