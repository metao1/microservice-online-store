package com.metao.product.infrustructure.factory.handler;

import java.util.Optional;

import com.metao.product.domain.ProductServiceInterface;
import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.mapper.ProductMapper;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductMessageHandler implements MessageHandler<CreateProductEvent> {

    private final ProductServiceInterface productService;
    private final ProductMapper productMapper;

    @Override
    public void onMessage(@NonNull CreateProductEvent  event) {
        var productDto = event.productDTO();        
        Optional.of(productDto)
                .flatMap(productMapper::toEntity)
                .ifPresent(productService::saveProduct);
    }

}