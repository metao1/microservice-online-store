package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDatabaseHandler implements Consumer<ProductCreatedEvent> {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Override
    public void accept(@NonNull ProductCreatedEvent event) {
        StageProcessor.accept(event)
            .map(productMapper::toEntity)
            .map(productEntity -> {
                productService.saveProduct(productEntity);
                return productEntity;
            }).acceptExceptionally((productEntity, exp) -> {
                if (exp != null) {
                    log.warn("saving product:{} , failed: {}", productEntity, exp.getMessage());
                } else {
                    log.info("saved product id:{}", productEntity.getAsin());
                }
            });
    }

}
