package com.metao.book.payment.presentation;

import com.metao.book.payment.application.dto.CreatePaymentCommand;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.usecase.PaymentUseCase;
import com.metao.book.payment.domain.service.PaymentDomainService;
import com.metao.book.shared.architecture.InboundAdapter;
import com.metao.book.shared.security.CurrentUser;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment operations
 */
@Slf4j
@RestController
@InboundAdapter(InboundAdapter.Kind.HTTP)
@RequiredArgsConstructor
@RequestMapping("/payments")
@Observed(name = "payment.api.controller", contextualName = "payment-controller")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    /**
     * Create a new payment
     */
    @PostMapping
    @Timed(value = "payment.api.create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_payments:write')")
    public PaymentDTO createPayment(@RequestBody CreatePaymentCommand command) {
        log.info("Creating payment for order: {}", command.orderId());
        String userId = CurrentUser.subject();
        // Include authenticated user ID in command for complete audit trail
        var commandWithUser = new CreatePaymentCommand(
            userId,
            command.orderId(),
            command.amount(),
            command.currency(),
            command.paymentMethodType(),
            command.paymentMethodDetails()
        );
        return paymentUseCase.createPayment(commandWithUser);
    }

    /**
     * Process a pending payment
     */
    @Timed(value = "payment.api.process")
    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasAuthority('SCOPE_payments:process')")
    public PaymentDTO processPayment(@PathVariable String paymentId) {
        log.info("Processing payment: {}", paymentId);
        return paymentUseCase.processPayment(paymentId);
    }

    /**
     * Retry a failed payment
     */
    @PostMapping("/{paymentId}/retry")
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_payments:write')")
    public PaymentDTO retryPayment(@PathVariable String paymentId) {
        log.info("Retrying payment: {}", paymentId);
        return paymentUseCase.retryPayment(paymentId);
    }

    /**
     * Cancel a payment
     */
    @PostMapping("/{paymentId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasAuthority('SCOPE_payments:write')")
    public void cancelPayment(@PathVariable String paymentId) {
        log.info("Cancelling payment: {}", paymentId);
        paymentUseCase.cancelPayment(paymentId);
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasAuthority('SCOPE_payments:read')")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable String paymentId) {
        log.debug("Getting payment: {}", paymentId);
        Optional<PaymentDTO> payment = paymentUseCase.getPaymentById(paymentId);
        return payment.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    @Timed(value = "payment.api.get-by-order-id")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasAuthority('SCOPE_payments:read')")
    public ResponseEntity<PaymentDTO> getPaymentInfoByOrderId(@PathVariable String orderId) {
        log.debug("Getting payment for order: {}", orderId);
        Optional<PaymentDTO> payment = paymentUseCase.getPaymentByOrderId(orderId);
        return payment.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_payments:read')")
    public List<PaymentDTO> getPaymentsByStatus(
        @PathVariable String status,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.debug("Getting payments by status: {}", status);
        return paymentUseCase.getPaymentsByStatus(status, offset, limit);
    }

    /**
     * Get payment statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_payments:read')")
    public PaymentDomainService.PaymentStatistics getPaymentStatistics() {
        log.debug("Getting payment statistics");
        return paymentUseCase.getPaymentStatistics();
    }
}
