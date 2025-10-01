package com.metao.book.payment.domain.service;

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
import java.util.List;
import lombok.NonNull;

/**
 * Domain service for complex business operations involving payments
 */
public record PaymentDomainService(PaymentRepository paymentRepository) {

    /**
     * Check if a payment can be created for an order
     */
    public boolean canCreatePaymentForOrder(@NonNull OrderId orderId) {
        return !paymentRepository.existsByOrderId(orderId);
    }

    /**
     * Create a new payment with business rules validation
     */
    public Payment createPayment(
        @NonNull OrderId orderId,
        @NonNull Money amount,
        @NonNull PaymentMethod paymentMethod
    ) {
        // Business rule: Only one payment per order
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new DuplicatePaymentException("Payment already exists for order: " + orderId);
        }

        // Business rule: Minimum payment amount
        if (amount.fixedPointAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new IllegalArgumentException("Payment amount must be at least 0.01");
        }

        // Business rule: Maximum payment amount (for security)
        if (amount.fixedPointAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed 10,000");
        }

        PaymentId paymentId = PaymentId.generate();
        return new Payment(paymentId, orderId, amount, paymentMethod);
    }

    /**
     * Process payment with business rules
     */
    public void processPayment(@NonNull PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Business rule: Check if payment can be processed
        if (!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new IllegalStateException("Payment must be in PENDING status to be processed");
        }

        payment.processPayment();
        paymentRepository.save(payment);
    }

    /**
     * Retry failed payment with business rules
     */
    public void retryPayment(@NonNull PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Business rule: Can only retry failed payments
        if (!payment.getStatus().canBeRetried()) {
            throw new IllegalStateException("Payment cannot be retried in current status: " + payment.getStatus());
        }

        payment.retry();
        paymentRepository.save(payment);
    }

    /**
     * Cancel payment with business rules
     */
    public void cancelPayment(@NonNull PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Business rule: Can only cancel pending payments
        if (!payment.getStatus().canBeCancelled()) {
            throw new IllegalStateException("Payment cannot be cancelled in current status: " + payment.getStatus());
        }

        payment.cancel();
        paymentRepository.save(payment);
    }

    /**
     * Check if payment method is valid for amount
     */
    public boolean isPaymentMethodValidForAmount(@NonNull PaymentMethod paymentMethod, @NonNull Money amount) {
        // Business rule: Card payments have different limits than digital payments
        if (paymentMethod.isCardPayment()) {
            // Card payments: max 5000
            return amount.fixedPointAmount().compareTo(BigDecimal.valueOf(5000)) <= 0;
        } else if (paymentMethod.isDigitalPayment()) {
            // Digital payments: max 2000
            return amount.fixedPointAmount().compareTo(BigDecimal.valueOf(2000)) <= 0;
        }

        // Bank transfers: max 10000
        return amount.fixedPointAmount().compareTo(BigDecimal.valueOf(10000)) <= 0;
    }

    /**
     * Get payment statistics
     */
    public PaymentStatistics getPaymentStatistics() {
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.countByStatus(PaymentStatus.SUCCESSFUL);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);

        return new PaymentStatistics(totalPayments, successfulPayments, failedPayments, pendingPayments);
    }

    /**
     * Find payments that need retry (failed payments)
     */
    public List<Payment> findPaymentsForRetry() {
        return paymentRepository.findByStatus(PaymentStatus.FAILED);
    }

    /**
     * Payment statistics value object
     */
    public record PaymentStatistics(
        long totalPayments,
        long successfulPayments,
        long failedPayments,
        long pendingPayments
    ) {

        public double getSuccessRate() {
            return totalPayments > 0 ? (double) successfulPayments / totalPayments * 100 : 0.0;
        }

        public double getFailureRate() {
            return totalPayments > 0 ? (double) failedPayments / totalPayments * 100 : 0.0;
        }
    }
}
