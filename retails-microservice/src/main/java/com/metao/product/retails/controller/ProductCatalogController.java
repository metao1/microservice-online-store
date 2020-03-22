package com.metao.product.retails.controller;

import com.metao.product.retails.model.ProductDTO;
import com.metao.product.retails.service.impl.ProductServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCatalogController implements ProductController {

    private final ProductServiceImplementation productService;

    @PostMapping(value = "/product", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        productService.saveProduct(productDTO);
    }

    @GetMapping(value = "/product/{asin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> getOneProduct(@PathVariable String asin) {
        return ResponseEntity.ok(productService.findProductById(asin));
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> getAllProductsWithOffset(@Param("limit") Integer limit,
                                                                     @Param("offset") Integer offset) {
        if (limit == null && offset == null) {
            return ResponseEntity.ok(productService.findAllProductsPageable(10, 0));
        }
        if (limit == null) {
            return ResponseEntity.ok(productService.findAllProductsPageable(10, offset));
        }
        if (offset == null) {
            return ResponseEntity.ok(productService.findAllProductsPageable(limit, 0));
        }
        if (limit <= 0 || offset < 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(productService.findAllProductsPageable(limit, offset));
    }

    @GetMapping(value = "/products/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable("category") String category,
                                                                  @Param("limit") int limit,
                                                                  @Param("offset") int offset) {
        return ResponseEntity.ok(productService.findAllProductsWithCategory(category, limit, offset));
    }
}
