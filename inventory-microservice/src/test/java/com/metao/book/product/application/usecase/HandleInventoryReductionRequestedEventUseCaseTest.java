package com.metao.book.product.application.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleInventoryReductionRequestedEventUseCaseTest {

    @Mock
    private ProductDomainService productService;

    @Mock
    private ProcessedInventoryEventPort processedInventoryEventPort;

    @Test
    void duplicateEventDoesNotReduceInventoryTwice() {
        var useCase = new HandleInventoryReductionRequestedEventUseCase(productService, processedInventoryEventPort);
        var command = new HandleInventoryReductionRequestedEventCommand(
            "event-1", "order-1", "SKU-1", BigDecimal.valueOf(2));
        when(processedInventoryEventPort.markProcessed("event-1")).thenReturn(true, false);

        useCase.handle(command);
        useCase.handle(command);

        verify(productService, times(1)).reduceProductVolumeAtomically(eq("SKU-1"), eq(BigDecimal.valueOf(2)));
    }
}
