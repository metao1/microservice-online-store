package com.metao.book.payment.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.payment.application.dto.CreatePaymentCommand;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.domain.service.PaymentDomainService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;

@ApplicationUseCase
public interface PaymentUseCase {

    PaymentDTO createPayment(@Valid CreatePaymentCommand command);

    PaymentDTO processPayment(String paymentId);

    PaymentDTO retryPayment(String paymentId);

    void cancelPayment(String paymentId);

    Optional<PaymentDTO> getPaymentById(String paymentId);

    Optional<PaymentDTO> getPaymentByOrderId(String orderId);

    List<PaymentDTO> getPaymentsByStatus(String status, int offset, int limit);

    PaymentDomainService.PaymentStatistics getPaymentStatistics();

    PaymentDTO processOrderCreatedEvent(String orderId, BigDecimal amount, String currency);
}
