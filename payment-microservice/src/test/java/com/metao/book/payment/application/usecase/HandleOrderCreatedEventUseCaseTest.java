package com.metao.book.payment.application.usecase;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.payment.application.dto.PaymentDTO;
import com.metao.book.payment.application.port.ProcessedOrderCreatedEventPort;
import com.metao.book.payment.application.service.PaymentApplicationService;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleOrderCreatedEventUseCaseTest {

    @Mock
    private PaymentApplicationService paymentApplicationService;

    @Mock
    private ProcessedOrderCreatedEventPort processedOrderCreatedEventPort;

    @InjectMocks
    private HandleOrderCreatedEventUseCase useCase;

    @Test
    void handleProcessesFirstOrderCreatedEvent() {
        HandleOrderCreatedEventCommand command = new HandleOrderCreatedEventCommand(
            "order-1",
            "order-1",
            BigDecimal.valueOf(10.0),
            "EUR"
        );

        PaymentDTO paymentDTO = PaymentDTO.builder()
            .paymentId("payment-1")
            .orderId("order-1")
            .amount(BigDecimal.valueOf(10.0))
            .currency(Currency.getInstance("EUR"))
            .status("SUCCESSFUL")
            .isCompleted(true)
            .isSuccessful(true)
            .build();

        when(processedOrderCreatedEventPort.markProcessed("order-1")).thenReturn(true);
        when(paymentApplicationService.processOrderCreatedEvent("order-1", BigDecimal.valueOf(10.0), "EUR"))
            .thenReturn(paymentDTO);

        useCase.handle(command);

        verify(paymentApplicationService).processOrderCreatedEvent("order-1", BigDecimal.valueOf(10.0), "EUR");
    }

    @Test
    void handleSkipsDuplicateOrderCreatedEvent() {
        HandleOrderCreatedEventCommand command = new HandleOrderCreatedEventCommand(
            "order-1",
            "order-1",
            BigDecimal.valueOf(10.0),
            "EUR"
        );

        when(processedOrderCreatedEventPort.markProcessed("order-1")).thenReturn(false);

        useCase.handle(command);

        verify(paymentApplicationService, never()).processOrderCreatedEvent("order-1", BigDecimal.valueOf(10.0), "EUR");
    }
}
