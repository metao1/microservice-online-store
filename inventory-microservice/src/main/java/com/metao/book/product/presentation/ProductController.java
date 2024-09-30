package com.metao.book.product.presentation;

import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.infrastructure.factory.producer.KafkaProductProducer;
import com.metao.book.shared.application.service.StageProcessor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductController {

    private final KafkaProductProducer kafkaProductProducer;
    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping(value = "/{asin}")
    public ResponseEntity<ProductDTO> productDetails(@PathVariable String asin) throws ProductNotFoundException {
        return productService.getProductByAsin(asin).map(productMapper::toDto).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/offset")
    public ResponseEntity<List<ProductDTO>> allProductsWithOffset(
        @RequestParam("limit") int limit, @RequestParam("offset") int offset
    ) {
        var l = Optional.of(limit).orElse(10);
        var o = Optional.of(offset).orElse(0);
        return productService.getAllProductsPageable(l, o).map(productMapper::toDTOList).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @SneakyThrows
    public ResponseEntity<String> saveProduct(@RequestBody ProductDTO productDTO) {
        return StageProcessor.accept(productDTO).map(productMapper::toEvent).applyExceptionally((event, exp) -> {
            try {
                return kafkaProductProducer.publish(event)
                    .thenApply(
                        ev -> ResponseEntity.status(HttpStatus.CREATED).body(ev.getProducerRecord().key())
                    )
                    .get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatusCode.valueOf(500)).body(e.getMessage());
            }
        });
    }
}
