package com.metao.product.presentation;

import com.metao.product.application.dto.ProductDTO;
import com.metao.product.application.exception.ProductNotFoundException;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.ProductServiceInterface;
import com.metao.product.infrustructure.mapper.ProductMapperInterface;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductServiceInterface productService;
    private final ProductMapperInterface productMapper;

    @PostMapping(value = "/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProduct(@Valid @RequestBody ProductDTO productDTO) {
        var pe = productMapper.toEntity(productDTO).orElseThrow();
        productService.saveProduct(pe);
    }

    @GetMapping(value = "/products/details/{asin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> getOneProduct(@PathVariable String asin) throws ProductNotFoundException{
        var productById = productService.getProductById(new ProductId(asin));
        return ResponseEntity.ok(productMapper.toDto(productById));
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> getAllProductsWithOffset(@RequestParam("limit") int limit,
                                                                     @RequestParam("offset") int offset) {
        var l = Optional.of(limit).orElse(10);
        var o = Optional.of(offset).orElse(0);
        var allPr = productService.getAllProductsPageable(l, o);
        return ResponseEntity.ok(productMapper.toDtos(allPr));
    }

//
//    @GetMapping(value = "/products/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable("category") String category,
//                                                                  @Param("limit") Integer limit,
//                                                                  @Param("offset") Integer offset) {
//
//        if (StringUtils.isEmpty(category)) {
//            return ResponseEntity.badRequest().build();
//        } else {
//            productService.getAllProductsWithCategory();
//            return ResponseEntity.ok(category, limit != null ? limit : 10,
//                    offset != null ? offset : 0));
//        }
//    }
//
//    @GetMapping(value = "/products/categories/{asin}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<ProductCategoriesDTO>> getAllProductCategories(@PathVariable("asin") String asin) {
//        return ResponseEntity.ok(productCategoriesService.getProductCategories(new ProductId(asin)));
//    }
}
