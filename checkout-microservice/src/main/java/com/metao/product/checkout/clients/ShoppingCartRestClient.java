package com.metao.product.checkout.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("card-ms")
@RequestMapping("/card")
public interface ShoppingCartRestClient {
	
	@RequestMapping("/addProduct")
    String addProductToCart(@RequestParam("userid") String userId,
                            @RequestParam("asin") String asin);

	@RequestMapping("/productsInCart")
    Map<String, Integer> getProductsInCart(@RequestParam("userid") String userId);

	@RequestMapping("/removeProduct")
    String removeProductFromCart(@RequestParam("userid") String userId,
                                 @RequestParam("asin") String asin);
	
	@RequestMapping("/clearCart")
    String clearCart(@RequestParam("userid") String userId);
}
