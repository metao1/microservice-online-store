package com.metao.book.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.service.PaymentApplicationService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.Status;
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

@Deprecated
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
            .setId("order-123")
            .setUserId("cust456")
            .addItems(OrderCreatedEvent.OrderItem.newBuilder()
                .setSku("prod789")
                .setProductTitle("Sample Product")
                .setPrice(10.0)
                .setQuantity(2.0)
                .setCurrency("USD")
                .build())
            .addItems(OrderCreatedEvent.OrderItem.newBuilder()
                .setSku("prod790")
                .setProductTitle("Sample Product 2")
                .setPrice(5.0)
                .setQuantity(1.0)
                .setCurrency("USD")
                .build())
            .setStatus(OrderCreatedEvent.Status.CREATED)
            .build();
    }

    @ParameterizedTest
    @CsvSource({
        "true,SUCCESSFUL,,payment-123",
        "false,FAILED,Insufficient funds,payment-456"
    })
    void processPayment_respectsServiceOutcome(
        boolean success,
        Status expectedStatus,
        String failureReason,
        String paymentId
    ) {
        OrderCreatedEvent orderEvent = createSampleOrderCreatedEvent();
        BigDecimal expectedTotal = BigDecimal.valueOf(25.0);

        PaymentDTO paymentDto = PaymentDTO.builder()
            .paymentId(paymentId)
            .orderId(orderEvent.getId())
            .amount(expectedTotal)
            .currency(Currency.getInstance("USD"))
            .status(expectedStatus.name())
            .failureReason(failureReason)
            .isSuccessful(success)
            .isCompleted(true)
            .createdAt(Instant.now())
            .build();

        when(paymentApplicationService.processOrderCreatedEvent(
            eq(orderEvent.getId()),
            argThat(amount -> amount != null && amount.compareTo(expectedTotal) == 0),
            eq("USD")
        )).thenReturn(paymentDto);

        PaymentDTO paymentEvent = paymentProcessingService.processPayment(orderEvent);

        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.orderId()).isEqualTo(orderEvent.getId());
        assertThat(paymentEvent.paymentId()).isEqualTo(paymentId);
        assertThat(paymentEvent.status()).isEqualTo(expectedStatus.name());
        assertThat(paymentEvent.createdAt()).isNotNull();
        if (success) {
            assertThat(paymentEvent.failureReason()).isNull();
        } else {
            assertThat(paymentEvent.failureReason()).contains("Insufficient funds");
        }

        verify(paymentApplicationService).processOrderCreatedEvent(
            eq(orderEvent.getId()),
            argThat(amount -> amount != null && amount.compareTo(expectedTotal) == 0),
            eq("USD")
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
        PaymentDTO paymentDTO = paymentProcessingService.processPayment(orderEvent);

        // Then
        assertThat(paymentDTO).isNull();
    }
}
