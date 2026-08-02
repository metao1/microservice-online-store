package com.metao.book.product.application.usecase;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.shared.architecture.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@ApplicationUseCase
@RequiredArgsConstructor
public class HandleInventoryReductionRequestedEventUseCase {

    private final ProductDomainService productService;
    private final ProcessedInventoryEventPort processedInventoryEventPort;

    @Transactional
    public void handle(HandleInventoryReductionRequestedEventCommand command) {
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new IllegalArgumentException("InventoryReductionRequestedEvent requires eventId");
        }
        if (command.sku() == null || command.sku().isBlank()) {
            throw new IllegalArgumentException("InventoryReductionRequestedEvent requires sku");
        }
        if (command.quantity() == null || command.quantity().signum() <= 0) {
            throw new IllegalArgumentException("InventoryReductionRequestedEvent requires positive quantity");
        }

        if (!processedInventoryEventPort.markProcessed(command.eventId())) {
            log.info("Inventory reduction event {} already processed, skipping.", command.eventId());
            return;
        }

        boolean reduced = productService.reduceProductVolumeAtomically(command.sku(), command.quantity());
        if (!reduced) {
            log.debug("Skipping inventory reduction for sku {} because stock is insufficient (event {}).",
                command.sku(), command.eventId());
            return;
        }

        log.info("Inventory reduced for sku {} by {} for order {} (event {}).",
            command.sku(), command.quantity(), command.orderId(), command.eventId());
    }
}
