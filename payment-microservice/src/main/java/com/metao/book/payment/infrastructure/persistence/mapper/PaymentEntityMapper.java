package com.metao.book.payment.infrastructure.persistence.mapper;

import com.metao.book.payment.domain.model.aggregate.Payment;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.infrastructure.persistence.entity.PaymentEntity;
import com.metao.book.shared.domain.financial.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper between Payment domain object and PaymentEntity
 */
@Component
public class PaymentEntityMapper {

    /**
     * Convert domain Payment to PaymentEntity
     */
    public PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(
            payment.getId().value(),
            payment.getOrderId().value(),
            payment.getAmount().fixedPointAmount(),
            payment.getAmount().currency(),
            mapPaymentMethodType(payment.getPaymentMethod().type()),
            payment.getPaymentMethod().details(),
            mapPaymentStatus(payment.getStatus()),
            payment.getCreatedAt()
        );
    }

    /**
     * Convert PaymentEntity to domain Payment
     */
    public Payment toDomain(PaymentEntity entity) {
        PaymentId paymentId = PaymentId.of(entity.getPaymentId());
        OrderId orderId = OrderId.of(entity.getOrderId());
        Money amount = new Money(entity.getCurrency(), entity.getAmount());
        PaymentMethod paymentMethod = mapPaymentMethod(entity.getPaymentMethodType(), entity.getPaymentMethodDetails());
        PaymentStatus status = mapPaymentStatus(entity.getStatus());

        return Payment.reconstruct(
            paymentId,
            orderId,
            amount,
            paymentMethod,
            status,
            entity.getFailureReason(),
            entity.getProcessedAt(),
            entity.getCreatedAt()
        );
    }

    /**
     * Map domain PaymentMethod.Type to entity PaymentMethodType
     */
    private PaymentEntity.PaymentMethodType mapPaymentMethodType(PaymentMethod.Type type) {
        return switch (type) {
            case CREDIT_CARD -> PaymentEntity.PaymentMethodType.CREDIT_CARD;
            case DEBIT_CARD -> PaymentEntity.PaymentMethodType.DEBIT_CARD;
            case PAYPAL -> PaymentEntity.PaymentMethodType.PAYPAL;
            case BANK_TRANSFER -> PaymentEntity.PaymentMethodType.BANK_TRANSFER;
            case DIGITAL_WALLET -> PaymentEntity.PaymentMethodType.DIGITAL_WALLET;
        };
    }

    /**
     * Map domain PaymentStatus to entity PaymentStatusEntity
     */
    private PaymentEntity.PaymentStatusEntity mapPaymentStatus(PaymentStatus status) {
        return switch (status) {
            case PENDING -> PaymentEntity.PaymentStatusEntity.PENDING;
            case SUCCESSFUL -> PaymentEntity.PaymentStatusEntity.SUCCESSFUL;
            case FAILED -> PaymentEntity.PaymentStatusEntity.FAILED;
            case CANCELLED -> PaymentEntity.PaymentStatusEntity.CANCELLED;
        };
    }

    /**
     * Map entity PaymentMethodType to domain PaymentMethod
     */
    private PaymentMethod mapPaymentMethod(PaymentEntity.PaymentMethodType type, String details) {
        return switch (type) {
            case CREDIT_CARD -> PaymentMethod.creditCard(details);
            case DEBIT_CARD -> PaymentMethod.debitCard(details);
            case PAYPAL -> PaymentMethod.paypal(details);
            case BANK_TRANSFER -> PaymentMethod.bankTransfer(details);
            case DIGITAL_WALLET -> PaymentMethod.digitalWallet(details);
        };
    }

    /**
     * Map entity PaymentStatusEntity to domain PaymentStatus
     */
    private PaymentStatus mapPaymentStatus(PaymentEntity.PaymentStatusEntity status) {
        return switch (status) {
            case PENDING -> PaymentStatus.PENDING;
            case SUCCESSFUL -> PaymentStatus.SUCCESSFUL;
            case FAILED -> PaymentStatus.FAILED;
            case CANCELLED -> PaymentStatus.CANCELLED;
        };
    }
}
