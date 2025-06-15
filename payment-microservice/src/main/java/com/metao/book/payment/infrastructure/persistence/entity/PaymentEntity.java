package com.metao.book.payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Payment persistence
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", nullable = false)
    private PaymentMethodType paymentMethodType;

    @Column(name = "payment_method_details")
    private String paymentMethodDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatusEntity status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PaymentEntity(
        String paymentId,
        String orderId,
        BigDecimal amount,
        Currency currency,
        PaymentMethodType paymentMethodType,
        String paymentMethodDetails,
        PaymentStatusEntity status,
        LocalDateTime createdAt
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodDetails = paymentMethodDetails;
        this.status = status;
        this.createdAt = createdAt;
    }

    public enum PaymentMethodType {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        BANK_TRANSFER,
        DIGITAL_WALLET
    }

    public enum PaymentStatusEntity {
        PENDING,
        SUCCESSFUL,
        FAILED,
        CANCELLED
    }
}
