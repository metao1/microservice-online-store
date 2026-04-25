package com.metao.book.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.application.dto.CreatePaymentCommand;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.mapper.PaymentApplicationMapper;
import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.payment.domain.service.PaymentDomainService;
import com.metao.book.shared.config.KafkaDomainEventPublisher;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Unit tests for PaymentApplicationService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentAggregateApplicationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDomainService paymentDomainService;

    @Mock
    private KafkaDomainEventPublisher eventPublisher;

    private PaymentApplicationService paymentApplicationService;

    @BeforeEach
    void setUp() {
        paymentApplicationService = new PaymentApplicationService(
            paymentRepository, paymentDomainService, eventPublisher
        );
        when(paymentRepository.findByOrderId(any(OrderId.class))).thenReturn(Optional.empty());
    }

    @Test
    void createPayment_withValidCommand_shouldCreateAndReturnPayment() {
        // Given
        CreatePaymentCommand command = new CreatePaymentCommand(
            "order-123",
            BigDecimal.valueOf(100.00),
            "EUR",
            PaymentMethod.Type.CREDIT_CARD,
            "****-1234"
        );

        PaymentAggregate payment = createPaymentAggregate(PaymentStatus.PENDING, null);
        PaymentDTO expectedDTO = PaymentApplicationMapper.toDTO(payment);

        when(paymentDomainService.isPaymentMethodValidForAmount(any(), any())).thenReturn(true);
        when(paymentDomainService.createPayment(any(), any(), any())).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // When
        PaymentDTO result = paymentApplicationService.createPayment(command);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(paymentDomainService).createPayment(any(OrderId.class), any(Money.class), any(PaymentMethod.class));
        verify(paymentRepository).save(payment);
        // Note: kafkaEventHandler.handle is only called if payment has domain events
    }

    @Test
    void createPayment_whenPaymentAlreadyExists_shouldReturnExistingPayment() {
        // Given
        CreatePaymentCommand command = new CreatePaymentCommand(
            "order-123",
            BigDecimal.valueOf(100.00),
            "EUR",
            PaymentMethod.Type.CREDIT_CARD,
            "****-1234"
        );

        PaymentAggregate existingPayment = createPaymentAggregate(PaymentStatus.PENDING, null);
        when(paymentRepository.findByOrderId(any(OrderId.class))).thenReturn(Optional.of(existingPayment));

        // When
        PaymentDTO result = paymentApplicationService.createPayment(command);

        // Then
        assertThat(result).isEqualTo(PaymentApplicationMapper.toDTO(existingPayment));
        verify(paymentDomainService, times(0)).createPayment(any(), any(), any());
        verify(paymentRepository, times(0)).save(any());
    }

    @Test
    void createPayment_whenConcurrentDuplicateInsertOccurs_shouldReturnExistingPayment() {
        // Given
        CreatePaymentCommand command = new CreatePaymentCommand(
            "order-123",
            BigDecimal.valueOf(100.00),
            "EUR",
            PaymentMethod.Type.CREDIT_CARD,
            "****-1234"
        );

        PaymentAggregate paymentToCreate = createPaymentAggregate(PaymentStatus.PENDING, null);
        PaymentAggregate existingPayment = createPaymentAggregate(PaymentStatus.PENDING, null);

        when(paymentDomainService.isPaymentMethodValidForAmount(any(), any())).thenReturn(true);
        when(paymentDomainService.createPayment(any(), any(), any())).thenReturn(paymentToCreate);
        when(paymentRepository.save(paymentToCreate))
            .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint uk_payment_order_id"));
        when(paymentRepository.findByOrderId(any(OrderId.class)))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingPayment));

        // When
        PaymentDTO result = paymentApplicationService.createPayment(command);

        // Then
        assertThat(result).isEqualTo(PaymentApplicationMapper.toDTO(existingPayment));
    }

    @Test
    void createPayment_withInvalidPaymentMethod_shouldThrowException() {
        // Given
        CreatePaymentCommand command = new CreatePaymentCommand(
            "order-123",
            BigDecimal.valueOf(100.00),
            "EUR",
            PaymentMethod.Type.CREDIT_CARD,
            "****-1234"
        );

        when(paymentDomainService.isPaymentMethodValidForAmount(any(), any())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> paymentApplicationService.createPayment(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment method not valid for this amount");
    }

    @Test
    void processPayment_withValidPaymentId_shouldProcessAndReturnPayment() {
        // Given
        String paymentId = "payment-123";
        PaymentAggregate payment = createPaymentAggregate(PaymentStatus.PENDING, null);
        payment.processPayment();
        PaymentDTO expectedDTO = PaymentApplicationMapper.toDTO(payment);

        when(paymentDomainService.processPayment(any(PaymentId.class))).thenReturn(payment);

        // When
        PaymentDTO result = paymentApplicationService.processPayment(paymentId);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(paymentDomainService).processPayment(any(PaymentId.class));
        verify(eventPublisher, times(1)).publish(any());
        assertThat(result.isSuccessful()).isTrue();
    }

    @Test
    void processPayment_whenAlreadySuccessful_shouldReturnExistingPaymentWithoutProcessing() {
        // Given
        String paymentId = "payment-123";
        PaymentAggregate existingPayment = createPaymentAggregate(PaymentStatus.SUCCESSFUL, null);
        PaymentDTO expectedDTO = PaymentApplicationMapper.toDTO(existingPayment);

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(existingPayment));

        // When
        PaymentDTO result = paymentApplicationService.processPayment(paymentId);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(paymentDomainService, times(0)).processPayment(any(PaymentId.class));
    }

    @Test
    void processPayment_whenConcurrentlyProcessed_shouldReturnLatestSuccessfulState() {
        // Given
        String paymentId = "payment-123";
        PaymentAggregate pendingPayment = createPaymentAggregate(PaymentStatus.PENDING, null);
        PaymentAggregate successfulPayment = createPaymentAggregate(PaymentStatus.SUCCESSFUL, null);
        PaymentDTO expectedDTO = PaymentApplicationMapper.toDTO(successfulPayment);

        when(paymentRepository.findById(any(PaymentId.class)))
            .thenReturn(Optional.of(pendingPayment))
            .thenReturn(Optional.of(successfulPayment));
        when(paymentDomainService.processPayment(any(PaymentId.class)))
            .thenThrow(new IllegalStateException("Payment must be in PENDING status to be processed"));

        // When
        PaymentDTO result = paymentApplicationService.processPayment(paymentId);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
    }

    @Test
    void getPaymentById_withExistingPayment_shouldReturnPayment() {
        // Given
        String paymentId = "payment-123";
        PaymentAggregate payment = createPaymentAggregate(PaymentStatus.SUCCESSFUL, null);
        PaymentDTO expectedDTO = PaymentApplicationMapper.toDTO(payment);

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(payment));

        // When
        Optional<PaymentDTO> result = paymentApplicationService.getPaymentById(paymentId);

        // Then
        assertThat(result)
            .isPresent()
            .get()
            .isEqualTo(expectedDTO);
    }

    @Test
    void getPaymentById_withNonExistentPayment_shouldReturnEmpty() {
        // Given
        String paymentId = "payment-123";
        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.empty());

        // When
        Optional<PaymentDTO> result = paymentApplicationService.getPaymentById(paymentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPaymentsByStatus_shouldReturnFilteredPayments() {
        // Given
        String status = "SUCCESSFUL";
        List<PaymentAggregate> payments = List.of(
            createPaymentAggregate(PaymentStatus.SUCCESSFUL, null),
            createPaymentAggregate(PaymentStatus.SUCCESSFUL, null)
        );

        when(paymentRepository.findByStatus(PaymentStatus.SUCCESSFUL, 0, 10)).thenReturn(payments);

        // When
        List<PaymentDTO> result = paymentApplicationService.getPaymentsByStatus(status, 0, 10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(paymentDTO -> assertThat(paymentDTO.status()).isEqualTo("SUCCESSFUL"));
    }

    @Test
    void retryPayment_withValidPaymentId_shouldRetryAndReturnPayment() {
        // Given
        String paymentId = "payment-123";
        PaymentAggregate payment = createPaymentAggregate(PaymentStatus.SUCCESSFUL, null);
        PaymentDTO expectedDTO = PaymentApplicationMapper.toDTO(payment);

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(payment));

        // When
        PaymentDTO result = paymentApplicationService.retryPayment(paymentId);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(paymentDomainService).retryPayment(any(PaymentId.class));
        // Note: kafkaEventHandler.handle is only called if payment has domain events
    }

    @Test
    void cancelPayment_withValidPaymentId_shouldCancelPayment() {
        // Given
        String paymentId = "payment-123";

        // When
        paymentApplicationService.cancelPayment(paymentId);

        // Then
        verify(paymentDomainService).cancelPayment(any(PaymentId.class));
    }

    @Test
    void processOrderCreatedEvent_shouldCreateAndProcessPayment() {
        // Given
        String orderId = "order-123";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String currency = "EUR";

        PaymentAggregate createdPayment = createPaymentAggregate(PaymentStatus.PENDING, null);
        PaymentAggregate processedPayment = createPaymentAggregate(PaymentStatus.SUCCESSFUL, null);
        PaymentDTO paymentDTO = PaymentApplicationMapper.toDTO(processedPayment);

        when(paymentDomainService.isPaymentMethodValidForAmount(any(), any())).thenReturn(true);
        when(paymentDomainService.createPayment(any(), any(), any())).thenReturn(createdPayment);
        when(paymentRepository.save(createdPayment)).thenReturn(createdPayment);
        when(paymentDomainService.processPayment(any(PaymentId.class))).thenReturn(processedPayment);

        // When
        PaymentDTO result = paymentApplicationService.processOrderCreatedEvent(orderId, amount, currency);

        // Then
        assertThat(result).isEqualTo(paymentDTO);
        verify(paymentDomainService).createPayment(any(), any(), any());
        verify(paymentDomainService).processPayment(any());
    }

    @Test
    void processOrderCreatedEvent_whenPaymentAlreadySuccessful_shouldSkipProcessing() {
        // Given
        String orderId = "order-123";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String currency = "EUR";

        PaymentAggregate successfulPayment = createPaymentAggregate(PaymentStatus.SUCCESSFUL, null);
        when(paymentRepository.findByOrderId(any(OrderId.class))).thenReturn(Optional.of(successfulPayment));

        // When
        PaymentDTO result = paymentApplicationService.processOrderCreatedEvent(orderId, amount, currency);

        // Then
        assertThat(result).isEqualTo(PaymentApplicationMapper.toDTO(successfulPayment));
        verify(paymentDomainService, times(0)).processPayment(any());
    }

    private PaymentAggregate createPaymentAggregate(PaymentStatus status, String failureReason) {
        PaymentAggregate payment = PaymentAggregate.reconstruct(
            PaymentId.of("payment-123"),
            OrderId.of("order-123"),
            Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(100.00)),
            PaymentMethod.creditCard("****-1234"),
            status,
            failureReason,
            status == PaymentStatus.PENDING ? null : Instant.now(),
            Instant.now().minusSeconds(60)
        );
        payment.clearDomainEvents();
        return payment;
    }
}
