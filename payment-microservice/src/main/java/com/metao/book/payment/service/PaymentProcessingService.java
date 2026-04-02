package com.metao.book.payment.service;

import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.service.PaymentApplicationService;
import com.metao.book.shared.OrderCreatedEvent;
import java.math.BigDecimal;
import java.util.Objects;
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
@Deprecated
@RequiredArgsConstructor
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentApplicationService paymentApplicationService;

    @Transactional
    public PaymentDTO processPayment(OrderCreatedEvent orderEvent) {
        var paymentAmount = resolveAmount(orderEvent);
        log.info("Processing payment for order: {}, amount: {} {}",
            orderEvent.getId(), paymentAmount.amount(), paymentAmount.currency());
        try {
            // Use the new DDD application service to process payment
            PaymentDTO payment = paymentApplicationService.processOrderCreatedEvent(
                orderEvent.getId(),
                paymentAmount.amount(),
                paymentAmount.currency()
            );
            String message = payment.isSuccessful()
                ? "Payment processed successfully"
                : payment.failureReason() != null
                    ? payment.failureReason()
                    : "Payment failed";

            log.info("Payment for order {}: {}", orderEvent.getId(), message);
            return payment;
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", orderEvent.getId(), e);
            return null;
        }
    }

    private PaymentAmount resolveAmount(OrderCreatedEvent orderEvent) {
        if (orderEvent.getItemsCount() == 0) {
            throw new IllegalArgumentException("OrderCreatedEvent must contain at least one item");
        }

        String currency = orderEvent.getItems(0).getCurrency();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreatedEvent.OrderItem item : orderEvent.getItemsList()) {
            if (!Objects.equals(currency, item.getCurrency())) {
                throw new IllegalArgumentException(
                    "OrderCreatedEvent contains mixed currencies: " + currency + " and " + item.getCurrency());
            }
            total = total.add(BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return new PaymentAmount(total, currency);
    }

    private record PaymentAmount(BigDecimal amount, String currency) {
    }
}
