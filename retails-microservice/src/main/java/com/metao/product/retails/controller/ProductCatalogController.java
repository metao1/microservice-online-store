package com.metao.product.retails.controller;

import com.metao.product.retails.model.ProductCategoriesDTO;
import com.metao.product.retails.model.ProductDTO;
import com.metao.product.retails.service.ProductCategoriesService;
import com.metao.product.retails.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductService productService;

    private final ProductCategoriesService productCategoriesService;

    @PostMapping(value = "/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        productService.saveProduct(productDTO);
    }

    @GetMapping(value = "/products/details/{asin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> getOneProduct(@PathVariable String asin) {
        return ResponseEntity.ok(productService.getProductById(asin));
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> getAllProductsWithOffset(@Param("limit") Integer limit,
                                                                     @Param("offset") Integer offset) {
        return ResponseEntity.ok(productService.getAllProductsPageable(limit != null ? limit : 10,
                offset != null ? offset : 0));
    }

    @GetMapping(value = "/products/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable("category") String category,
                                                                  @Param("limit") Integer limit,
                                                                  @Param("offset") Integer offset) {
        if (StringUtils.isEmpty(category)) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(productService.getAllProductsWithCategory(category, limit != null ? limit : 10,
                    offset != null ? offset : 0));
        }
    }

    @GetMapping(value = "/products/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductCategoriesDTO>> getAllProductCategories() {
        return ResponseEntity.ok(productCategoriesService.getProductCategories());
    }
}