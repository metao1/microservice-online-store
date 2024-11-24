package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.event.ProductUpdatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDatabaseHandler {

    private final ProductService productService;

    public void accept(@NonNull ProductCreatedEvent event) {
        StageProcessor.accept(event)
            .map(ProductMapper::fromProductCreatedEvent)
            .acceptExceptionally((productEntity, exp) -> {
                if (exp != null && productEntity == null) {
                    log.warn("saving product, failed: {}", exp.getMessage());
                } else {
                    productService.saveProduct(productEntity);
                    log.info("saved product id:{}", productEntity.getAsin());
                }
            });
    }

    public void accept(@NonNull ProductUpdatedEvent event) {
        StageProcessor.accept(event)
            .map(ProductMapper::fromProductUpdatedEvent)
            .acceptExceptionally((productEntity, exp) -> {
                if (exp != null && productEntity == null) {
                    log.warn("saving product id:{}, failed: {}", event.getAsin(), exp.getMessage());
                } else {
                    productService.saveProduct(productEntity);
                    log.info("saved product id:{}", productEntity.getAsin());
                }
            });
    }

}
