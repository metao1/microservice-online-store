package com.metao.book.product.application.usecase;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.shared.architecture.ApplicationService;
import com.metao.book.shared.architecture.ApplicationUseCase;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@ApplicationService
@ApplicationUseCase
public class HandleProductUpdatedEventUseCase {

    private final ProductDomainService productService;
    private final ProcessedInventoryEventPort processedInventoryEventPort;

    @Autowired
    public HandleProductUpdatedEventUseCase(
        ProductDomainService productService,
        ProcessedInventoryEventPort processedInventoryEventPort
    ) {
        this.productService = productService;
        this.processedInventoryEventPort = processedInventoryEventPort;
    }

    @Transactional
    public void handle(HandleProductUpdatedEventCommand command) {
        if (!"INVENTORY_REDUCTION".equals(command.description())) {
            return;
        }

        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new IllegalArgumentException("Inventory reduction event requires eventId");
        }
        if (command.sku() == null || command.sku().isBlank()) {
            throw new IllegalArgumentException("Inventory reduction event requires sku");
        }
        BigDecimal quantity = command.volume();
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Inventory reduction event requires positive quantity");
        }
        if (!processedInventoryEventPort.markProcessed(command.eventId())) {
            log.info("Inventory reduction event {} already processed, skipping.", command.eventId());
            return;
        }

        productService.reduceProductVolumeAtomically(command.sku(), quantity);
        log.info("Product updated event received for SKU: {}", command.sku());
    }
}
