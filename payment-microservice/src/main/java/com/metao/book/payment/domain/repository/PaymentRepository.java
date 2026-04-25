package com.metao.book.payment.domain.repository;

import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Payment aggregate
 */
public interface PaymentRepository {

    /**
     * Save a payment
     */
    PaymentAggregate save(PaymentAggregate payment);

    /**
     * Save a payment and flush changes to database immediately.
     */
    default PaymentAggregate saveAndFlush(PaymentAggregate payment) {
        return save(payment);
    }

    /**
     * Acquire a DB-backed lock for payment creation on a specific order ID.
     */
    default void lockOrderForCreation(OrderId orderId) {
        // Default no-op for non-database-backed implementations.
    }

    /**
     * Find payment by ID
     */
    Optional<PaymentAggregate> findById(PaymentId paymentId);

    /**
     * Find payment by ID with row lock for write operations.
     */
    Optional<PaymentAggregate> findByIdForUpdate(PaymentId paymentId);

    /**
     * Find payment by order ID
     */
    Optional<PaymentAggregate> findByOrderId(OrderId orderId);

    /**
     * Find payments by status
     */
    List<PaymentAggregate> findByStatus(PaymentStatus status);

    /**
     * Find payments by status with pagination
     */
    List<PaymentAggregate> findByStatus(PaymentStatus status, int offset, int limit);

    /**
     * Find all payments with pagination
     */
    List<PaymentAggregate> findAll(int offset, int limit);

    /**
     * Check if payment exists for order
     */
    boolean existsByOrderId(OrderId orderId);

    /**
     * Delete payment
     */
    void delete(PaymentAggregate payment);

    /**
     * Count total payments
     */
    long count();

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);
}
