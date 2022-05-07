package com.metao.book.retails.presentation;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.application.exception.ProductNotFoundException;
import com.metao.book.retails.domain.ProductId;
import com.metao.book.retails.domain.ProductServiceInterface;
import com.metao.book.retails.infrustructure.mapper.ProductMapperInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceInterface productService;
    private final ProductMapperInterface productMapper;

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        Optional.of(productMapper.toEntity(productDTO))
                .orElseThrow()
                .ifPresent(productService::saveProduct);
    }

    @GetMapping(value = "/details/{asin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> getOneProduct(@PathVariable String asin) throws ProductNotFoundException {
        return productService.getProductById(new ProductId(asin))
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/offset", produces = MediaType.APPLICATION_JSON_VALUE)
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
