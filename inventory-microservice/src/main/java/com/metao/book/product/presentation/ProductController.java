package com.metao.book.product.presentation;

import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductController {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final ProductService productService;

    @GetMapping(value = "/{asin}")
    public ProductDTO productDetails(@PathVariable String asin) throws ProductNotFoundException {
        return productService.getProductByAsin(asin).map(ProductMapper::toDto)
            .orElseThrow(() -> new ProductNotFoundException("product " + asin + " not found."));
    }

    @GetMapping
    public List<ProductDTO> allProductsWithOffset(
        @RequestParam("limit") int limit, @RequestParam("offset") int offset
    ) {
        var l = Optional.of(limit).orElse(10);
        var o = Optional.of(offset).orElse(0);
        return productService.getAllProductsPageable(l, o).map(ProductMapper::toDto).toList();
    }

    @PostMapping
    @SneakyThrows
    @ResponseStatus(HttpStatus.CREATED)
    public boolean saveProduct(
        @RequestBody ProductDTO productDTO,
        @Value("${kafka.topic.product-created.name}") String productTopic)
    {
        return StageProcessor.accept(productDTO)
            .map(ProductMapper::toProductCreatedEvent)
            .applyExceptionally((event, exp) -> {
                if (exp != null && event == null) {
                    log.warn("invalid event when saving product {}", exp.toString());
                    return false;
                }
                try {
                    kafkaTemplate.send(productTopic, event.getAsin(), event).get(10, TimeUnit.SECONDS);
                    return true;
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    log.error("error when saving product {}", e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
    }

    @GetMapping("/category/{name}")
    public List<ProductDTO> productsByCategory(
        @PathVariable("name") String name, @RequestParam("offset") int offset, @RequestParam("limit") int limit
    ) {
        return productService.getProductsByCategory(limit, offset, name).map(ProductMapper::toDto).toList();
    }
}
