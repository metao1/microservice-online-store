package com.metao.book.payment.application.mapper;

import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.domain.model.aggregate.Payment;
import org.springframework.stereotype.Component;

/**
 * Mapper between domain objects and application DTOs
 */
@Component
public class PaymentApplicationMapper {

    /**
     * Convert domain Payment to PaymentDTO
     */
    public PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
            .paymentId(payment.getId().value())
            .orderId(payment.getOrderId().value())
            .amount(payment.getAmount().fixedPointAmount())
            .currency(payment.getAmount().currency())
            .paymentMethodType(payment.getPaymentMethod().type().name())
            .paymentMethodDetails(payment.getPaymentMethod().details())
            .status(payment.getStatus().name())
            .failureReason(payment.getFailureReason())
            .processedAt(payment.getProcessedAt())
            .createdAt(payment.getCreatedAt())
            .isCompleted(payment.getStatus().isCompleted())
            .isSuccessful(payment.getStatus().isSuccessful())
            .build();
    }
}
