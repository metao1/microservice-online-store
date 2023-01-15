package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.ProductServiceInterface;
import com.metao.book.product.domain.event.CreateProductEvent;
import com.metao.book.product.infrastructure.mapper.ProductMapperInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDatabaseHandler implements MessageHandler<CreateProductEvent> {

    private final ProductServiceInterface productService;
    private final ProductMapperInterface productMapper;

    @Override
    public void onMessage(@NonNull CreateProductEvent event) {
        try {
            log.info("event occurred on: {}", event.occurredOn());
            var productDto = event.productDTO();
            Optional.of(productDto)
                .flatMap(productMapper::toEntity)
                .ifPresent(productService::saveProduct);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }
    }

}
