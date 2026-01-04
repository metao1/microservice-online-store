package com.metao.book.payment.domain.model.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.metao.book.payment.domain.model.event.PaymentFailedEvent;
import com.metao.book.payment.domain.model.event.PaymentProcessedEvent;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Payment aggregate
 */
class PaymentAggregateTest {

    @Test
    void createPayment_shouldInitializeWithPendingStatus() {
        // Given
        PaymentId paymentId = PaymentId.generate();
        OrderId orderId = OrderId.of("order-123");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(100.00));
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");

        // When
        PaymentAggregate payment = new PaymentAggregate(paymentId, orderId, amount, paymentMethod);

        // Then
        assertThat(payment.getId()).isEqualTo(paymentId);
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getProcessedAt()).isNull();
        assertThat(payment.getFailureReason()).isNull();
        assertThat(payment.getDomainEvents()).isEmpty();
    }

    @Test
    void processPayment_withValidAmount_shouldSucceedOrFail() {
        // Given
        PaymentAggregate payment = createValidPayment();

        // When
        payment.processPayment();

        // Then
        assertThat(payment.getStatus()).isIn(PaymentStatus.SUCCESSFUL, PaymentStatus.FAILED);
        assertThat(payment.getProcessedAt()).isNotNull();
        assertThat(payment.getDomainEvents()).hasSize(1);

        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            assertThat(payment.getDomainEvents().getFirst()).isInstanceOf(PaymentProcessedEvent.class);
            assertThat(payment.getFailureReason()).isNull();
        } else {
            assertThat(payment.getDomainEvents().getFirst()).isInstanceOf(PaymentFailedEvent.class);
            assertThat(payment.getFailureReason()).isNotNull();
        }
    }

    @Test
    void processPayment_withNonPendingStatus_shouldThrowException() {
        // Given
        PaymentAggregate payment = createValidPayment();
        payment.processPayment(); // Process once to change status

        // When/Then
        assertThatThrownBy(payment::processPayment)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Payment can only be processed when in PENDING status");
    }

    @Test
    void processPayment_withZeroAmount_shouldThrowException() {
        // Given
        PaymentId paymentId = PaymentId.generate();
        OrderId orderId = OrderId.of("order-123");
        Money zeroAmount = new Money(Currency.getInstance("USD"), BigDecimal.ZERO);
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");
        PaymentAggregate payment = new PaymentAggregate(paymentId, orderId, zeroAmount, paymentMethod);

        // When/Then
        assertThatThrownBy(payment::processPayment)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment amount must be positive");
    }

    @Test
    void retry_withFailedPayment_shouldResetToPending() {
        // Given
        PaymentAggregate payment = createValidPayment();
        payment.processPayment();

        // Ensure it's failed (retry until we get a failed payment for testing)
        while (payment.getStatus() != PaymentStatus.FAILED) {
            payment = createValidPayment();
            payment.processPayment();
        }

        payment.clearDomainEvents(); // Clear events from initial processing

        // When
        payment.retry();

        // Then
        assertThat(payment.getStatus()).isIn(PaymentStatus.SUCCESSFUL, PaymentStatus.FAILED);
        assertThat(payment.getDomainEvents()).isNotEmpty();
    }

    @Test
    void retry_withNonFailedPayment_shouldThrowException() {
        // Given
        PaymentAggregate payment = createValidPayment();

        // When/Then
        assertThatThrownBy(payment::retry)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only retry failed payments");
    }

    @Test
    void cancel_withPendingPayment_shouldSetStatusToCancelled() {
        // Given
        PaymentAggregate payment = createValidPayment();

        // When
        payment.cancel();

        // Then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getProcessedAt()).isNotNull();
    }

    @Test
    void cancel_withNonPendingPayment_shouldThrowException() {
        // Given
        PaymentAggregate payment = createValidPayment();
        payment.processPayment(); // Change status from PENDING

        // When/Then
        assertThatThrownBy(payment::cancel)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only cancel pending payments");
    }

    @Test
    void reconstruct_shouldCreatePaymentWithGivenState() {
        // Given
        PaymentId paymentId = PaymentId.of("payment-123");
        OrderId orderId = OrderId.of("order-456");
        Money amount = new Money(Currency.getInstance("EUR"), BigDecimal.valueOf(50.00));
        PaymentMethod paymentMethod = PaymentMethod.paypal("test@example.com");
        PaymentStatus status = PaymentStatus.SUCCESSFUL;
        String failureReason = null;
        var processedAt = Instant.now();
        var createdAt = processedAt.minus(5, ChronoUnit.MINUTES);

        // When
        PaymentAggregate payment = PaymentAggregate.reconstruct(
            paymentId, orderId, amount, paymentMethod, status,
            failureReason, processedAt, createdAt
        );

        // Then
        assertThat(payment.getId()).isEqualTo(paymentId);
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(payment.getStatus()).isEqualTo(status);
        assertThat(payment.getFailureReason()).isEqualTo(failureReason);
        assertThat(payment.getProcessedAt()).isEqualTo(processedAt);
        assertThat(payment.getCreatedAt()).isEqualTo(createdAt);
        assertThat(payment.getDomainEvents()).isEmpty();
    }

    private PaymentAggregate createValidPayment() {
        PaymentId paymentId = PaymentId.generate();
        OrderId orderId = OrderId.of("order-123");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(100.00));
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");
        return new PaymentAggregate(paymentId, orderId, amount, paymentMethod);
    }
}
