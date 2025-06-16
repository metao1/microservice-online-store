package com.metao.book.payment.presentation;

import com.metao.book.payment.application.dto.CreatePaymentCommand;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.service.PaymentApplicationService;
import com.metao.book.payment.domain.service.PaymentDomainService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment operations
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    /**
     * Create a new payment
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDTO createPayment(@Valid @RequestBody CreatePaymentCommand command) {
        log.info("Creating payment for order: {}", command.orderId());
        return paymentApplicationService.createPayment(command);
    }

    /**
     * Process a payment
     */
    @PostMapping("/{paymentId}/process")
    public PaymentDTO processPayment(@PathVariable String paymentId) {
        log.info("Processing payment: {}", paymentId);
        return paymentApplicationService.processPayment(paymentId);
    }

    /**
     * Retry a failed payment
     */
    @PostMapping("/{paymentId}/retry")
    public PaymentDTO retryPayment(@PathVariable String paymentId) {
        log.info("Retrying payment: {}", paymentId);
        return paymentApplicationService.retryPayment(paymentId);
    }

    /**
     * Cancel a payment
     */
    @PostMapping("/{paymentId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelPayment(@PathVariable String paymentId) {
        log.info("Cancelling payment: {}", paymentId);
        paymentApplicationService.cancelPayment(paymentId);
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable String paymentId) {
        log.debug("Getting payment: {}", paymentId);
        Optional<PaymentDTO> payment = paymentApplicationService.getPaymentById(paymentId);
        return payment.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDTO> getPaymentByOrderId(@PathVariable String orderId) {
        log.debug("Getting payment for order: {}", orderId);
        Optional<PaymentDTO> payment = paymentApplicationService.getPaymentByOrderId(orderId);
        return payment.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    public List<PaymentDTO> getPaymentsByStatus(@PathVariable String status) {
        log.debug("Getting payments by status: {}", status);
        return paymentApplicationService.getPaymentsByStatus(status);
    }

    /**
     * Get payment statistics
     */
    @GetMapping("/statistics")
    public PaymentDomainService.PaymentStatistics getPaymentStatistics() {
        log.debug("Getting payment statistics");
        return paymentApplicationService.getPaymentStatistics();
    }
}
