package com.metao.book.product.presentation;

import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.product.infrastructure.factory.handler.ProductEventHandler;
import com.metao.book.product.infrastructure.util.EventUtil;
import com.metao.book.shared.application.service.StageProcessor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductCreationController {

    private final ProductEventHandler productEventHandler;

    @PostMapping
    public ResponseEntity<Void> saveProduct(@Valid @RequestBody ProductEvent productEvent) {
        return StageProcessor.accept(productEvent)
            .map(EventUtil::createProductEvent)
            .applyExceptionally((event, exp) -> {
                if (event != null && exp == null) {
                    productEventHandler.publish(event);
                    return ResponseEntity.status(HttpStatus.CREATED).build();
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            });
    }

}
