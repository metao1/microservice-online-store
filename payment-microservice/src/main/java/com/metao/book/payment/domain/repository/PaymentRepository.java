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
     * Find payment by ID
     */
    Optional<PaymentAggregate> findById(PaymentId paymentId);

    /**
     * Find payment by order ID
     */
    Optional<PaymentAggregate> findByOrderId(OrderId orderId);

    /**
     * Find payments by status
     */
    List<PaymentAggregate> findByStatus(PaymentStatus status);

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
