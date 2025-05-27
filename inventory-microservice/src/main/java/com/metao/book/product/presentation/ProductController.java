package com.metao.book.product.presentation;

import com.metao.book.product.application.service.kafkaProductProducer;
import com.metao.book.product.application.service.kafkaProductProducer;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import java.util.Arrays; // Added import
import java.util.Collections; // Added import
import java.util.List;
import java.util.stream.Collectors; // Added import
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Already present, but good to confirm
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductService productService;
    private final kafkaProductProducer kafkaProductProducer;

    @GetMapping(value = "/{asin}")
    public ProductDTO productDetails(@PathVariable String asin) throws ProductNotFoundException {
        return productService.getProductByAsin(asin).map(ProductMapper::toDto)
            .orElseThrow(() -> new ProductNotFoundException("product " + asin + " not found."));
    }

    @PostMapping
    @SneakyThrows
    @ResponseStatus(HttpStatus.CREATED)
    public boolean saveProduct(
        @RequestBody ProductDTO productDTO
    )
    {
        return kafkaProductProducer.sendEvent(ProductMapper.toProductCreatedEvent(productDTO));
    }

    @GetMapping("/categories/{categoriesCsv}")
    public List<ProductDTO> productsByCategories(
        @PathVariable("categoriesCsv") String categoriesCsv,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        List<String> categoryList;
        if (categoriesCsv == null || categoriesCsv.trim().isEmpty()) {
            categoryList = java.util.Collections.emptyList();
        } else {
            categoryList = java.util.Arrays.stream(categoriesCsv.split(","))
                                     .map(String::trim)
                                     .filter(s -> !s.isEmpty())
                                     .collect(java.util.stream.Collectors.toList());
        }
        return productService.getProductsByCategories(limit, offset, categoryList);
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return productService.searchProductsByKeyword(keyword, offset, limit);
    }
}
