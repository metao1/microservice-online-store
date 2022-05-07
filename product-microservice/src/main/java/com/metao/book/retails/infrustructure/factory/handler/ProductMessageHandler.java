package com.metao.book.retails.infrustructure.factory.handler;

import com.metao.book.retails.domain.ProductServiceInterface;
import com.metao.book.retails.domain.event.CreateProductEvent;
import com.metao.book.retails.infrustructure.mapper.ProductMapperInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
