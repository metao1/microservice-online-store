package com.metao.book.payment.application.service;

import com.metao.book.payment.application.dto.CreatePaymentCommand;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.mapper.PaymentApplicationMapper;
import com.metao.book.payment.domain.exception.PaymentNotFoundException;
import com.metao.book.payment.domain.model.aggregate.Payment;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.payment.domain.service.PaymentDomainService;
import com.metao.book.shared.domain.financial.Money;
import com.metao.kafka.KafkaEventHandler;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for Payment operations - orchestrates domain services and repositories
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;
    private final PaymentApplicationMapper paymentMapper;
    private final KafkaEventHandler kafkaEventHandler;

    /**
     * Create a new payment
     */
    public PaymentDTO createPayment(CreatePaymentCommand command) {
        log.info("Creating payment for order: {}", command.orderId());

        // Use domain service to create payment with business rules
        OrderId orderId = OrderId.of(command.orderId());
        Money amount = new Money(Currency.getInstance(command.currency()), command.amount());
        PaymentMethod paymentMethod = createPaymentMethod(command);

        // Validate payment method for amount
        if (!paymentDomainService.isPaymentMethodValidForAmount(paymentMethod, amount)) {
            throw new IllegalArgumentException("Payment method not valid for this amount");
        }

        Payment payment = paymentDomainService.createPayment(orderId, amount, paymentMethod);
        Payment savedPayment = paymentRepository.save(payment);

        // Publish domain events to Kafka
        publishDomainEvents(savedPayment);

        log.info("Payment created successfully with ID: {}", savedPayment.getId());
        return paymentMapper.toDTO(savedPayment);
    }

    /**
     * Process a payment
     */
    public PaymentDTO processPayment(String id) {
        log.info("Processing payment: {}", id);

        PaymentId paymentId = PaymentId.of(id);
        paymentDomainService.processPayment(paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Publish domain events to Kafka
        publishDomainEvents(payment);

        log.info("Payment processed successfully: {} with status: {}", id, payment.getStatus());
        return paymentMapper.toDTO(payment);
    }

    /**
     * Retry a failed payment
     */
    public PaymentDTO retryPayment(String id) {
        log.info("Retrying payment: {}", id);

        PaymentId paymentId = PaymentId.of(id);
        paymentDomainService.retryPayment(paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        log.info("Payment retry completed: {} with status: {}", id, payment.getStatus());
        return paymentMapper.toDTO(payment);
    }

    /**
     * Cancel a payment
     */
    public void cancelPayment(String paymentId) {
        log.info("Cancelling payment: {}", paymentId);

        PaymentId id = PaymentId.of(paymentId);
        paymentDomainService.cancelPayment(id);

        log.info("Payment cancelled successfully: {}", paymentId);
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> getPaymentById(String paymentId) {
        log.debug("Getting payment by ID: {}", paymentId);

        PaymentId id = PaymentId.of(paymentId);
        return paymentRepository.findById(id)
            .map(paymentMapper::toDTO);
    }

    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> getPaymentByOrderId(String orderId) {
        log.debug("Getting payment by order ID: {}", orderId);

        OrderId id = OrderId.of(orderId);
        return paymentRepository.findByOrderId(id)
            .map(paymentMapper::toDTO);
    }

    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(String status) {
        log.debug("Getting payments by status: {}", status);

        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        List<Payment> payments = paymentRepository.findByStatus(paymentStatus);
        return payments.stream()
            .map(paymentMapper::toDTO)
            .toList();
    }

    /**
     * Get payment statistics using domain service
     */
    @Transactional(readOnly = true)
    public PaymentDomainService.PaymentStatistics getPaymentStatistics() {
        log.debug("Getting payment statistics");
        return paymentDomainService.getPaymentStatistics();
    }

    /**
     * Process order created event (from order microservice)
     */
    public PaymentDTO processOrderCreatedEvent(String orderId, BigDecimal amount, String currency) {
        log.info("Processing order created event for order: {}", orderId);

        CreatePaymentCommand command = new CreatePaymentCommand(
            orderId,
            amount,
            currency,
            PaymentMethod.Type.CREDIT_CARD,
            "****-****-****-1234"
        );

        PaymentDTO payment = createPayment(command);

        // Auto-process the payment
        processPayment(payment.paymentId());

        return this.getPaymentById(payment.paymentId()).orElseThrow();
    }

    /**
     * Helper method to create PaymentMethod from command
     */
    private PaymentMethod createPaymentMethod(CreatePaymentCommand command) {
        return switch (command.paymentMethodType()) {
            case CREDIT_CARD -> PaymentMethod.creditCard(command.paymentMethodDetails());
            case DEBIT_CARD -> PaymentMethod.debitCard(command.paymentMethodDetails());
            case PAYPAL -> PaymentMethod.paypal(command.paymentMethodDetails());
            case BANK_TRANSFER -> PaymentMethod.bankTransfer(command.paymentMethodDetails());
            case DIGITAL_WALLET -> PaymentMethod.digitalWallet(command.paymentMethodDetails());
        };
    }

    /**
     * Publish domain events from payment aggregate to Kafka
     */
    private void publishDomainEvents(Payment payment) {
        if (payment == null || !payment.hasDomainEvents()) {
            return;
        }

        payment.getDomainEvents().forEach(event -> {
            try {
                // Use KafkaEventHandler to publish domain events
                kafkaEventHandler.send(payment.getId().value(), event);
                log.debug("Published domain event: {} for payment: {}",
                    event.getEventType(), payment.getId().value());
            } catch (Exception e) {
                log.error("Failed to publish domain event: {} for payment: {}",
                    event.getEventType(), payment.getId().value(), e);
                // Don't rethrow - we don't want to break the main business flow
            }
        });

        // Clear events after publishing
        payment.clearDomainEvents();
    }
}
