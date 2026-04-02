package com.metao.book.product.application.usecase;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleProductCreatedEventUseCase {

    private final ProcessedInventoryEventPort processedInventoryEventPort;

    @Transactional
    public void handle(HandleProductCreatedEventCommand command) {
        if (command.eventId() == null || command.eventId().isBlank()) {
            log.warn("Skipping ProductCreatedEvent without idempotency key for sku {}", command.sku());
            return;
        }

        boolean firstProcessing = processedInventoryEventPort.markProcessed(command.eventId());
        if (!firstProcessing) {
            log.info("Product created event {} already processed, skipping.", command.eventId());
            return;
        }

        log.info("Product created event received for SKU: {}", command.sku());
    }
}
