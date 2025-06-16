package com.metao.book.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.application.dto.CreatePaymentCommand;
import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.mapper.PaymentApplicationMapper;
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
import java.time.LocalDateTime;
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

/**
 * Unit tests for PaymentApplicationService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentApplicationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDomainService paymentDomainService;

    @Mock
    private PaymentApplicationMapper paymentMapper;

    @Mock
    private KafkaEventHandler kafkaEventHandler;

    private PaymentApplicationService paymentApplicationService;

    @BeforeEach
    void setUp() {
        paymentApplicationService = new PaymentApplicationService(
            paymentRepository, paymentDomainService, paymentMapper, kafkaEventHandler
        );
    }

    @Test
    void createPayment_withValidCommand_shouldCreateAndReturnPayment() {
        // Given
        CreatePaymentCommand command = new CreatePaymentCommand(
            "order-123",
            BigDecimal.valueOf(100.00),
            Currency.getInstance("USD"),
            PaymentMethod.Type.CREDIT_CARD,
            "****-1234"
        );

        Payment payment = createMockPayment();
        PaymentDTO expectedDTO = createMockPaymentDTO();

        when(paymentDomainService.isPaymentMethodValidForAmount(any(), any())).thenReturn(true);
        when(paymentDomainService.createPayment(any(), any(), any())).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDTO(payment)).thenReturn(expectedDTO);

        // When
        PaymentDTO result = paymentApplicationService.createPayment(command);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(paymentDomainService).createPayment(any(OrderId.class), any(Money.class), any(PaymentMethod.class));
        verify(paymentRepository).save(payment);
        // Note: kafkaEventHandler.handle is only called if payment has domain events
    }

    @Test
    void createPayment_withInvalidPaymentMethod_shouldThrowException() {
        // Given
        CreatePaymentCommand command = new CreatePaymentCommand(
            "order-123",
            BigDecimal.valueOf(100.00),
            Currency.getInstance("USD"),
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
        Payment payment = createMockPayment();
        PaymentDTO expectedDTO = createMockPaymentDTO();

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(expectedDTO);

        // When
        PaymentDTO result = paymentApplicationService.processPayment(paymentId);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(paymentDomainService).processPayment(any(PaymentId.class));
        // Note: kafkaEventHandler.handle is only called if payment has domain events
        verify(paymentRepository).findById(any(PaymentId.class));
        assertTrue(paymentMapper.toDTO(payment).isSuccessful());
    }

    @Test
    void getPaymentById_withExistingPayment_shouldReturnPayment() {
        // Given
        String paymentId = "payment-123";
        Payment payment = createMockPayment();
        PaymentDTO expectedDTO = createMockPaymentDTO();

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(expectedDTO);

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
        List<Payment> payments = List.of(createMockPayment(), createMockPayment());
        PaymentDTO expectedDTO = createMockPaymentDTO();

        when(paymentRepository.findByStatus(PaymentStatus.SUCCESSFUL)).thenReturn(payments);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(expectedDTO);

        // When
        List<PaymentDTO> result = paymentApplicationService.getPaymentsByStatus(status);

        // Then
        assertThat(result).hasSize(2);
        verify(paymentMapper, times(2)).toDTO(any(Payment.class));
    }

    @Test
    void retryPayment_withValidPaymentId_shouldRetryAndReturnPayment() {
        // Given
        String paymentId = "payment-123";
        Payment payment = createMockPayment();
        PaymentDTO expectedDTO = createMockPaymentDTO();

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(expectedDTO);

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
        String currency = "USD";

        Payment payment = createMockPayment();
        PaymentDTO paymentDTO = createMockPaymentDTO();

        when(paymentDomainService.isPaymentMethodValidForAmount(any(), any())).thenReturn(true);
        when(paymentDomainService.createPayment(any(), any(), any())).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentRepository.findById(any())).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentDTO);

        // When
        PaymentDTO result = paymentApplicationService.processOrderCreatedEvent(orderId, amount, currency);

        // Then
        assertThat(result).isEqualTo(paymentDTO);
        verify(paymentDomainService).createPayment(any(), any(), any());
        verify(paymentDomainService).processPayment(any());
    }

    private Payment createMockPayment() {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(PaymentId.of("payment-123"));
        when(payment.getDomainEvents()).thenReturn(List.of());
        when(payment.hasDomainEvents()).thenReturn(false);
        return payment;
    }

    private PaymentDTO createMockPaymentDTO() {
        return PaymentDTO.builder()
            .paymentId("payment-123")
            .orderId("order-123")
            .amount(BigDecimal.valueOf(100.00))
            .currency(Currency.getInstance("USD"))
            .status("SUCCESSFUL")
            .isSuccessful(true)
            .isCompleted(true)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
