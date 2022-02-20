package com.metao.product.checkout.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("product-ms")
@RequestMapping("/products")
public interface ProductCatalogRestClient {

  @GetMapping("/details/{asin}")
  ProductDTO getProductDetails(@PathVariable("asin") String asin);

  @GetMapping
  List<ProductDTO> getProducts(@RequestParam("limit") int limit, @RequestParam("offset") int offset);

  @GetMapping("/category/{category}")
  List<ProductDTO> getProductsByCategory(@PathVariable("category") String category,
                                         @RequestParam("limit") int limit,
                                         @RequestParam("offset") int offset);
}
