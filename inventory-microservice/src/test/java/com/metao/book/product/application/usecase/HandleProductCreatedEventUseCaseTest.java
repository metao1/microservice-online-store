package com.metao.book.product.application.usecase;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleProductCreatedEventUseCaseTest {

    @Mock
    private ProcessedInventoryEventPort processedInventoryEventPort;

    @InjectMocks
    private HandleProductCreatedEventUseCase useCase;

    @Test
    void handleMarksFirstProductCreatedEventAsProcessed() {
        when(processedInventoryEventPort.markProcessed("event-1")).thenReturn(true);

        useCase.handle(new HandleProductCreatedEventCommand("event-1", "SKU-1"));

        verify(processedInventoryEventPort).markProcessed("event-1");
    }

    @Test
    void handleSkipsProductCreatedEventWithoutIdempotencyKey() {
        useCase.handle(new HandleProductCreatedEventCommand(" ", "SKU-1"));

        verify(processedInventoryEventPort, never()).markProcessed(" ");
    }
}
