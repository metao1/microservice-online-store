package com.metao.product.infrustructure.factory;

import com.metao.product.domain.ProductService;
import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMessageHandler {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public void onCreateProductEvent(@NonNull CreateProductEvent createProductEvent) {
        var productDto = createProductEvent.getProductDTO();
        Objects.requireNonNull(productDto, "productDto can't be null");

        Optional.of(productDto)
                .flatMap(productMapper::toEntity)
                .ifPresent(productService::saveProduct);
    }

}
