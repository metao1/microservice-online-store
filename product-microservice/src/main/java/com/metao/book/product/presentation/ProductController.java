package com.metao.book.product.presentation;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.exception.ProductNotFoundException;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductServiceInterface;
import com.metao.book.product.infrastructure.mapper.ProductMapperInterface;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@RequestMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductServiceInterface productService;
    private final ProductMapperInterface productMapper;

    @PostMapping(value = "/")
    public void saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        Optional.of(productMapper.toEntity(productDTO))
                .orElseThrow()
                .ifPresent(productService::saveProduct);
    }

    @GetMapping(value = "/details/{asin}")
    public ResponseEntity<ProductDTO> getOneProduct(@PathVariable ProductId asin) throws ProductNotFoundException {
        return productService.getProductById(asin)
                .map(ProductEntity::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/offset")
    public ResponseEntity<List<ProductDTO>> getAllProductsWithOffset(@RequestParam("limit") int limit,
                                                                     @RequestParam("offset") int offset) {
        var l = Optional.of(limit).orElse(10);
        var o = Optional.of(offset).orElse(0);
        return productService.getAllProductsPageable(l, o)
                .map(productMapper::toDtos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
