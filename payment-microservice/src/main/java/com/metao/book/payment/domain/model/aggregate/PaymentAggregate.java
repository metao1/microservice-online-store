package com.metao.book.payment.domain.model.aggregate;

import com.metao.book.payment.domain.model.event.PaymentFailedEvent;
import com.metao.book.payment.domain.model.event.PaymentProcessedEvent;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.domain.exception.PaymentStateTransitionNotAllowed;
import com.metao.book.shared.domain.base.AggregateRoot;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;

/**
 * Payment aggregate root - contains all business logic for payment processing
 */
@Getter
public class PaymentAggregate extends AggregateRoot<PaymentId> {

    private OrderId orderId;
    private Money amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String failureReason;
    private Instant processedAt;
    private Instant createdAt;
    private Long version;

    // For reconstruction from persistence
    protected PaymentAggregate() {
        super();
    }

    // Constructor for new payments
    public PaymentAggregate(
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
    public static PaymentAggregate reconstruct(
        PaymentId paymentId,
        OrderId orderId,
        Money amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String failureReason,
        Instant processedAt,
        Instant createdAt
    ) {
        PaymentAggregate payment = new PaymentAggregate();
        payment.setId(paymentId);
        payment.orderId = orderId;
        payment.amount = amount;
        payment.paymentMethod = paymentMethod;
        payment.status = status;
        payment.failureReason = failureReason;
        payment.processedAt = processedAt;
        payment.createdAt = createdAt;
        payment.version = null;
        return payment;
    }

    /**
     * For reconstruction from persistence, including optimistic-lock version.
     */
    public static PaymentAggregate reconstruct(
        PaymentId paymentId,
        OrderId orderId,
        Money amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String failureReason,
        Instant processedAt,
        Instant createdAt,
        Long version
    ) {
        PaymentAggregate payment = new PaymentAggregate();
        payment.setId(paymentId);
        payment.orderId = orderId;
        payment.amount = amount;
        payment.paymentMethod = paymentMethod;
        payment.status = status;
        payment.failureReason = failureReason;
        payment.processedAt = processedAt;
        payment.createdAt = createdAt;
        payment.version = version;
        return payment;
    }

    /**
     * Process the payment - core business logic
     */
    public void processPayment() {
        processPayment(true, null);
    }

    public void processPayment(boolean processingSuccessful, String failureReason) {
        PaymentStatus targetStatus = processingSuccessful ? PaymentStatus.SUCCESSFUL : PaymentStatus.FAILED;
        if (!status.canTransitionTo(targetStatus)) {
            throw new PaymentStateTransitionNotAllowed(status, targetStatus);
        }

        if (!isAmountValid()) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (processingSuccessful) {
            markAsSuccessful();
        } else {
            markAsFailed(failureReason != null ? failureReason : "Payment gateway authorization failed");
        }
    }

    /**
     * Mark payment as successful
     */
    private void markAsSuccessful() {
        transitionTo(PaymentStatus.SUCCESSFUL);
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
        transitionTo(PaymentStatus.FAILED);
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
        transitionTo(PaymentStatus.PENDING);
        this.failureReason = null;
        this.processedAt = null;

        // Process again
        processPayment();
    }

    /**
     * Cancel pending payment
     */
    public void cancel() {
        transitionTo(PaymentStatus.CANCELLED);
        this.processedAt = Instant.now();
    }

    /**
     * Business rule: Check if amount is valid
     */
    private boolean isAmountValid() {
        return amount != null &&
            amount.fixedPointAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private void transitionTo(PaymentStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new PaymentStateTransitionNotAllowed(status, target);
        }
        this.status = target;
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
        if (!(obj instanceof PaymentAggregate that)) {
            return false;
        }
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
