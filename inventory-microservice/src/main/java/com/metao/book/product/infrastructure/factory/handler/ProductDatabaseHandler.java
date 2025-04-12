package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.service.EventValidator;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductDatabaseHandler {

    private final ProductService productService;
    private final EventValidator validator;

    public void accept(@NonNull ProductCreatedEvent event) {
        Errors errors = validator.validateObject(event);
        if (errors.hasErrors()) {
            errors.getAllErrors().forEach(err -> log.warn("validation error: {}", err.getObjectName()));
            return;
        }
        StageProcessor.accept(event)
            .map(ProductMapper::fromProductCreatedEvent)
            .acceptExceptionally((productEntity, exp) -> {
                if (exp != null && productEntity == null) {
                    exp.printStackTrace();
                    log.warn("saving product, failed: {}", exp.getMessage());
                } else {
                    var saved = productService.saveProduct(productEntity);
                    log.info("{} product id:{}", saved ? "saved" : "not saved", productEntity.getAsin());
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
