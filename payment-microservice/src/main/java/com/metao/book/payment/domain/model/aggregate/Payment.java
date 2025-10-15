package com.metao.book.payment.domain.model.aggregate;

import com.metao.book.payment.domain.model.event.PaymentFailedEvent;
import com.metao.book.payment.domain.model.event.PaymentProcessedEvent;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.shared.domain.base.AggregateRoot;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;

/**
 * Payment aggregate root - contains all business logic for payment processing
 */
@Getter
public class Payment extends AggregateRoot<PaymentId> {

    private OrderId orderId;
    private Money amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String failureReason;
    private Instant processedAt;
    private Instant createdAt;

    // For reconstruction from persistence
    protected Payment() {
        super();
    }

    // Constructor for new payments
    public Payment(
        @NonNull PaymentId paymentId,
        @NonNull OrderId orderId,
        @NonNull Money amount,
        @NonNull PaymentMethod paymentMethod
    ) {
        super(paymentId);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    /**
     * For reconstruction from persistence
     */
    public static Payment reconstruct(
        PaymentId paymentId,
        OrderId orderId,
        Money amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String failureReason,
        Instant processedAt,
        Instant createdAt
    ) {
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.orderId = orderId;
        payment.amount = amount;
        payment.paymentMethod = paymentMethod;
        payment.status = status;
        payment.failureReason = failureReason;
        payment.processedAt = processedAt;
        payment.createdAt = createdAt;
        return payment;
    }

    /**
     * Process the payment - core business logic
     */
    public void processPayment() {
        if (!status.equals(PaymentStatus.PENDING)) {
            throw new IllegalStateException("Payment can only be processed when in PENDING status");
        }

        if (!isAmountValid()) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        // Simulate payment processing logic
        boolean processingSuccessful = simulatePaymentProcessing();

        if (processingSuccessful) {
            markAsSuccessful();
        } else {
            markAsFailed("Insufficient funds or payment gateway error");
        }
    }

    /**
     * Mark payment as successful
     */
    private void markAsSuccessful() {
        this.status = PaymentStatus.SUCCESSFUL;
        this.processedAt = Instant.now();
        this.failureReason = null;

        // Raise domain event
        addDomainEvent(new PaymentProcessedEvent(
            this.getId(),
            this.orderId,
            this.amount,
            this.status
        ));
    }

    /**
     * Mark payment as failed
     */
    private void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.processedAt = Instant.now();
        this.failureReason = reason;

        // Raise domain event
        addDomainEvent(new PaymentFailedEvent(
            this.getId(),
            this.orderId,
            this.amount,
            reason
        ));
    }

    /**
     * Retry failed payment
     */
    public void retry() {
        if (!status.canBeRetried()) {
            throw new IllegalStateException("Can only retry failed payments");
        }

        this.status = PaymentStatus.PENDING;
        this.failureReason = null;
        this.processedAt = null;

        // Process again
        processPayment();
    }

    /**
     * Cancel pending payment
     */
    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException("Can only cancel pending payments");
        }

        this.status = PaymentStatus.CANCELLED;
        this.processedAt = Instant.now();
    }

    /**
     * Business rule: Check if amount is valid
     */
    private boolean isAmountValid() {
        return amount != null &&
            amount.fixedPointAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * Simulate payment processing (replace with real payment gateway integration)
     */
    private boolean simulatePaymentProcessing() {
        // Simulate processing delay and random success/failure
        try {
            Thread.sleep(100); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        // 80% success rate for simulation
        return Math.random() > 0.2;
    }

    /**
     * Check if payment is completed (successful or failed)
     */
    public boolean isCompleted() {
        return status.equals(PaymentStatus.SUCCESSFUL) ||
            status.equals(PaymentStatus.FAILED) ||
            status.equals(PaymentStatus.CANCELLED);
    }

    /**
     * Check if payment was successful
     */
    public boolean isSuccessful() {
        return status.equals(PaymentStatus.SUCCESSFUL);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Payment that)) {
            return false;
        }
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
