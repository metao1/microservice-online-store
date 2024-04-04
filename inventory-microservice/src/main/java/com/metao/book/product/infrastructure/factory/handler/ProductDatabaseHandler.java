package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.config.ProductMapService;
import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.event.ProductCreatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDatabaseHandler implements MessageHandler<ProductCreatedEvent> {

    private final ProductService productService;
    private final ProductMapService productMapper;

    @Override
    public void onMessage(@NonNull ProductCreatedEvent event) {
        log.info("event occurred on: {}", event.occurredOn());
        StageProcessor.accept(event.productEvent())
            .thenApply(productMapper::toEntity)
            .acceptExceptionally((entity, exp) -> {
                if (exp != null) {
                    log.warn(exp.getMessage());
                } else {
                    productService.saveProduct(entity);
                }
            });
    }

}
