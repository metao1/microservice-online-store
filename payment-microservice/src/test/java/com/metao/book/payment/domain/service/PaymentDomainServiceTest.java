package com.metao.book.payment.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.domain.exception.DuplicatePaymentException;
import com.metao.book.payment.domain.exception.PaymentNotFoundException;
import com.metao.book.payment.domain.model.aggregate.Payment;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for PaymentDomainService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentDomainServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentDomainService paymentDomainService;

    @BeforeEach
    void setUp() {
        paymentDomainService = new PaymentDomainService(paymentRepository);
    }

    @Test
    void canCreatePaymentForOrder_whenNoExistingPayment_shouldReturnTrue() {
        // Given
        OrderId orderId = OrderId.of("order-123");
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        // When
        boolean result = paymentDomainService.canCreatePaymentForOrder(orderId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canCreatePaymentForOrder_whenPaymentExists_shouldReturnFalse() {
        // Given
        OrderId orderId = OrderId.of("order-123");
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

        // When
        boolean result = paymentDomainService.canCreatePaymentForOrder(orderId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void createPayment_withValidData_shouldCreatePayment() {
        // Given
        OrderId orderId = OrderId.of("order-123");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(100.00));
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        // When
        Payment payment = paymentDomainService.createPayment(orderId, amount, paymentMethod);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void createPayment_whenPaymentAlreadyExists_shouldThrowException() {
        // Given
        OrderId orderId = OrderId.of("order-123");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(100.00));
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> paymentDomainService.createPayment(orderId, amount, paymentMethod))
            .isInstanceOf(DuplicatePaymentException.class)
            .hasMessageContaining("Payment already exists for order");
    }

    @Test
    void createPayment_withAmountTooSmall_shouldThrowException() {
        // Given
        OrderId orderId = OrderId.of("order-123");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(0.005)); // Less than 0.01
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> paymentDomainService.createPayment(orderId, amount, paymentMethod))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment amount must be at least 0.01");
    }

    @Test
    void createPayment_withAmountTooLarge_shouldThrowException() {
        // Given
        OrderId orderId = OrderId.of("order-123");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(15000)); // More than 10,000
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> paymentDomainService.createPayment(orderId, amount, paymentMethod))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment amount cannot exceed 10,000");
    }

    @Test
    void processPayment_withValidPayment_shouldProcessAndSave() {
        // Given
        PaymentId paymentId = PaymentId.of("payment-123");
        Payment payment = createMockPayment(paymentId, PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        // When
        paymentDomainService.processPayment(paymentId);

        // Then
        verify(payment).processPayment();
        verify(paymentRepository).save(payment);
    }

    @Test
    void processPayment_withNonExistentPayment_shouldThrowException() {
        // Given
        PaymentId paymentId = PaymentId.of("payment-123");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentDomainService.processPayment(paymentId))
            .isInstanceOf(PaymentNotFoundException.class)
            .hasMessageContaining("Payment not found");
    }

    @Test
    void isPaymentMethodValidForAmount_withCardPaymentUnderLimit_shouldReturnTrue() {
        // Given
        PaymentMethod cardMethod = PaymentMethod.creditCard("****-1234");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(3000));

        // When
        boolean result = paymentDomainService.isPaymentMethodValidForAmount(cardMethod, amount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isPaymentMethodValidForAmount_withCardPaymentOverLimit_shouldReturnFalse() {
        // Given
        PaymentMethod cardMethod = PaymentMethod.creditCard("****-1234");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(6000));

        // When
        boolean result = paymentDomainService.isPaymentMethodValidForAmount(cardMethod, amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPaymentMethodValidForAmount_withDigitalPaymentUnderLimit_shouldReturnTrue() {
        // Given
        PaymentMethod digitalMethod = PaymentMethod.paypal("user@example.com");
        Money amount = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(1500));

        // When
        boolean result = paymentDomainService.isPaymentMethodValidForAmount(digitalMethod, amount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void getPaymentStatistics_shouldReturnCorrectStatistics() {
        // Given
        when(paymentRepository.count()).thenReturn(100L);
        when(paymentRepository.countByStatus(PaymentStatus.SUCCESSFUL)).thenReturn(80L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(15L);
        when(paymentRepository.countByStatus(PaymentStatus.PENDING)).thenReturn(5L);

        // When
        PaymentDomainService.PaymentStatistics stats = paymentDomainService.getPaymentStatistics();

        // Then
        assertThat(stats.totalPayments()).isEqualTo(100L);
        assertThat(stats.successfulPayments()).isEqualTo(80L);
        assertThat(stats.failedPayments()).isEqualTo(15L);
        assertThat(stats.pendingPayments()).isEqualTo(5L);
        assertThat(stats.getSuccessRate()).isEqualTo(80.0);
        assertThat(stats.getFailureRate()).isEqualTo(15.0);
    }

    @Test
    void findPaymentsForRetry_shouldReturnFailedPayments() {
        // Given
        List<Payment> failedPayments = List.of(
            createMockPayment(PaymentId.of("payment-1"), PaymentStatus.FAILED),
            createMockPayment(PaymentId.of("payment-2"), PaymentStatus.FAILED)
        );
        when(paymentRepository.findByStatus(PaymentStatus.FAILED)).thenReturn(failedPayments);

        // When
        List<Payment> result = paymentDomainService.findPaymentsForRetry();

        // Then
        assertThat(result).hasSize(2);
        verify(paymentRepository).findByStatus(PaymentStatus.FAILED);
    }

    private Payment createMockPayment(PaymentId paymentId, PaymentStatus status) {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getStatus()).thenReturn(status);
        return payment;
    }
}
