package com.metao.book.product.presentation;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.product.infrastructure.util.EventUtil;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<Void> saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        Optional.of(productDTO).map(EventUtil::createProductEvent).ifPresent(productKafkaHandler::onMessage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
