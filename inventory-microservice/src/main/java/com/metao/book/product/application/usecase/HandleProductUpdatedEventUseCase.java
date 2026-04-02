package com.metao.book.product.application.usecase;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleProductUpdatedEventUseCase {

    private static final String INVENTORY_REDUCTION_MARKER = "INVENTORY_REDUCTION";

    private final ProductDomainService productService;
    private final ProcessedInventoryEventPort processedInventoryEventPort;

    @Transactional
    public void handle(HandleProductUpdatedEventCommand command) {
        if (command.eventId() == null || command.eventId().isBlank()) {
            log.warn("Skipping ProductUpdatedEvent without idempotency key for sku {}", command.sku());
            return;
        }

        if (!INVENTORY_REDUCTION_MARKER.equals(command.description())) {
            log.info("Product updated event received for SKU: {}", command.sku());
            return;
        }

        boolean firstProcessing = processedInventoryEventPort.markProcessed(command.eventId());
        if (!firstProcessing) {
            log.info("Inventory reduction event {} already processed, skipping.", command.eventId());
            return;
        }

        productService.reduceProductVolumeAtomically(command.sku(), command.volume());

        log.info("Inventory reduced for sku {} by {} (event {}).",
            command.sku(), command.volume(), command.eventId());
    }
}
