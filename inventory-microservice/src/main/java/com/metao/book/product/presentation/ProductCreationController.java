package com.metao.book.product.presentation;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.product.infrastructure.mapper.ProductEventMapper;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductCreationController {

    private final ProductKafkaHandler productKafkaHandler;
    private final ProductEventMapper productMapper;

    @PostMapping
    public ResponseEntity<String> saveProduct(@RequestBody ProductDTO productDTO) {
        return StageProcessor.accept(productDTO)
            .map(productMapper::toEvent)
            .applyExceptionally((event, exp) -> {
                if (event != null && exp == null) {
                    productKafkaHandler.accept(event);
                    return ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(event.getAsin());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            });
    }

}
