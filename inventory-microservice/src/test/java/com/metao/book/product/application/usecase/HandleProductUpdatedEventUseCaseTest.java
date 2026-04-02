package com.metao.book.product.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HandleProductUpdatedEventUseCase")
class HandleProductUpdatedEventUseCaseTest {

    @Mock
    private ProductDomainService productService;

    @Mock
    private ProcessedInventoryEventPort processedInventoryEventPort;

    @InjectMocks
    private HandleProductUpdatedEventUseCase useCase;

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("should reduce inventory for inventory-reduction marker event")
        void shouldReduceInventoryForInventoryReductionMarkerEvent() {
            HandleProductUpdatedEventCommand command = new HandleProductUpdatedEventCommand(
                "order-1:SKU-1",
                "SKU-1",
                "INVENTORY_REDUCTION",
                BigDecimal.valueOf(3.0)
            );

            when(processedInventoryEventPort.markProcessed("order-1:SKU-1")).thenReturn(true);

            useCase.handle(command);

            verify(productService).reduceProductVolumeAtomically(eq("SKU-1"), eq(BigDecimal.valueOf(3.0)));
        }

        @Test
        @DisplayName("should skip duplicate inventory-reduction event")
        void shouldSkipDuplicateInventoryReductionEvent() {
            HandleProductUpdatedEventCommand command = new HandleProductUpdatedEventCommand(
                "order-1:SKU-1",
                "SKU-1",
                "INVENTORY_REDUCTION",
                BigDecimal.valueOf(3.0)
            );

            when(processedInventoryEventPort.markProcessed("order-1:SKU-1")).thenReturn(false);

            useCase.handle(command);

            verify(productService, never()).reduceProductVolumeAtomically(any(), any());
        }

        @Test
        @DisplayName("should ignore non-inventory product-updated events")
        void shouldIgnoreNonInventoryProductUpdatedEvents() {
            HandleProductUpdatedEventCommand command = new HandleProductUpdatedEventCommand(
                "any-key",
                "SKU-1",
                "NORMAL_PRODUCT_UPDATE",
                BigDecimal.valueOf(3.0)
            );

            useCase.handle(command);

            verify(processedInventoryEventPort, never()).markProcessed(any());
            verify(productService, never()).reduceProductVolumeAtomically(any(), any());
        }
    }
}
