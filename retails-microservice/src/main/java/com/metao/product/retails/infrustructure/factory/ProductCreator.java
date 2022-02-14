package com.metao.product.retails.infrustructure.factory;

import com.metao.product.retails.domain.product.ProductService;
import com.metao.product.retails.domain.product.event.CreateProductEvent;
import com.metao.product.retails.infrustructure.mapper.ProductMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ProductCreator {

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
