package com.metao.book.product.application.usecase;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.shared.architecture.ApplicationService;
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

    @Autowired
    public HandleProductUpdatedEventUseCase(ProductDomainService productService) {
        this.productService = productService;
    }

    public HandleProductUpdatedEventUseCase(
        ProductDomainService productService,
        ProcessedInventoryEventPort ignoredProcessedInventoryEventPort
    ) {
        this.productService = productService;
    }

    @Transactional
    public void handle(HandleProductUpdatedEventCommand command) {
        log.info("Product updated event received for SKU: {}", command.sku());
    }
}
