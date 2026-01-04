package com.metao.book.payment.service;

import com.google.protobuf.Timestamp;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.service.PaymentApplicationService;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.book.shared.OrderPaymentEvent.Status;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Legacy service that bridges the old interface with the new DDD structure This maintains backward compatibility while
 * using the new domain model
 */
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentApplicationService paymentApplicationService;

    @Transactional
    public OrderPaymentEvent processPayment(OrderCreatedEvent orderEvent) {
        log.info("Processing payment for order: {}, amount: {} {}",
            orderEvent.getId(), orderEvent.getPrice(), orderEvent.getCurrency());

        try {
            // Use the new DDD application service to process payment
            PaymentDTO payment = paymentApplicationService.processOrderCreatedEvent(
                orderEvent.getId(),
                java.math.BigDecimal.valueOf(orderEvent.getPrice()),
                orderEvent.getCurrency()
            );

            // Convert to legacy OrderPaymentEvent format
            Status status = payment.isSuccessful() ? Status.SUCCESSFUL : Status.FAILED;
            String message = payment.isSuccessful()
                ? "Payment processed successfully"
                : payment.failureReason() != null
                    ? payment.failureReason()
                    : "Payment failed";

            log.info("Payment for order {}: {}", orderEvent.getId(), message);

            return OrderPaymentEvent.newBuilder()
                .setOrderId(orderEvent.getId())
                .setPaymentId(payment.paymentId())
                .setStatus(status)
                .setErrorMessage(payment.isSuccessful() ? "" : message)
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();

        } catch (Exception e) {
            log.error("Error processing payment for order: {}", orderEvent.getId(), e);

            // Return failed payment event
            return OrderPaymentEvent.newBuilder()
                .setOrderId(orderEvent.getId())
                .setPaymentId(orderEvent.getId())
                .setStatus(Status.FAILED)
                .setErrorMessage("Payment processing failed: " + e.getMessage())
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();
        }
    }
}
