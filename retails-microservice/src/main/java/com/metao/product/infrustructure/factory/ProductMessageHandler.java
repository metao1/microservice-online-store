package com.metao.product.infrustructure.factory;

import java.util.Optional;

import com.metao.product.domain.ProductService;
import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.factory.handler.MessageHandler;
import com.metao.product.infrustructure.mapper.ProductMapper;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductMessageHandler implements MessageHandler<CreateProductEvent> {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Override
    public void onMessage(@NonNull CreateProductEvent  event) {
        var productDto = event.getProductDTO();        
        Optional.of(productDto)
                .flatMap(productMapper::toEntity)
                .ifPresent(productService::saveProduct);
    }

}
