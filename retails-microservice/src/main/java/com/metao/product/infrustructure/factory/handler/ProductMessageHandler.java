package com.metao.product.infrustructure.factory.handler;

import java.util.Optional;

import com.metao.product.domain.ProductServiceInterface;
import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.mapper.ProductMapperInterface;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMessageHandler implements MessageHandler<CreateProductEvent> {

    private final ProductServiceInterface productService;
    private final ProductMapperInterface productMapper;

    @Override
    public void onMessage(@NonNull CreateProductEvent event) {
        try {
            var productDto = event.productDTO();
            Optional.of(productDto)
                    .flatMap(productMapper::toEntity)
                    .ifPresent(productService::saveProduct);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }
    }

}
