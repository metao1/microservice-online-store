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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentAggregateProcessingServiceTest {

    @Mock
    private PaymentApplicationService paymentApplicationService;

    private PaymentProcessingService paymentProcessingService;

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

    @ParameterizedTest
    @CsvSource({
        "true,SUCCESSFUL,,payment-123",
        "false,FAILED,Insufficient funds,payment-456"
    })
    void processPayment_respectsServiceOutcome(
        boolean success,
        OrderPaymentEvent.Status expectedStatus,
        String failureReason,
        String paymentId
    ) {
        OrderCreatedEvent orderEvent = createSampleOrderCreatedEvent();

        PaymentDTO paymentDto = PaymentDTO.builder()
            .paymentId(paymentId)
            .orderId(orderEvent.getId())
            .amount(BigDecimal.valueOf(orderEvent.getPrice()))
            .currency(Currency.getInstance(orderEvent.getCurrency()))
            .status(expectedStatus.name())
            .failureReason(failureReason)
            .isSuccessful(success)
            .isCompleted(true)
            .createdAt(Instant.now())
            .build();

        when(paymentApplicationService.processOrderCreatedEvent(
            orderEvent.getId(),
            BigDecimal.valueOf(orderEvent.getPrice()),
            orderEvent.getCurrency()
        )).thenReturn(paymentDto);

        OrderPaymentEvent paymentEvent = paymentProcessingService.processPayment(orderEvent);

        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getOrderId()).isEqualTo(orderEvent.getId());
        assertThat(paymentEvent.getPaymentId()).isEqualTo(paymentId);
        assertThat(paymentEvent.getStatus()).isEqualTo(expectedStatus);
        assertThat(paymentEvent.getCreateTime()).isNotNull();
        if (success) {
            assertThat(paymentEvent.getErrorMessage()).isEmpty();
        } else {
            assertThat(paymentEvent.getErrorMessage()).contains("Insufficient funds");
        }

        verify(paymentApplicationService).processOrderCreatedEvent(
            orderEvent.getId(),
            BigDecimal.valueOf(orderEvent.getPrice()),
            orderEvent.getCurrency()
        );
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
        assertThat(paymentEvent.getPaymentId()).startsWith("orderItem123");
        assertThat(paymentEvent.getStatus()).isEqualTo(OrderPaymentEvent.Status.FAILED);
        assertThat(paymentEvent.getErrorMessage()).contains("Payment processing failed");
    }
}
