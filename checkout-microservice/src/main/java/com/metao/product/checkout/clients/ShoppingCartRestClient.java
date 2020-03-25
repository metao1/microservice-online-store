package com.metao.product.checkout.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("cart-ms")
public interface ShoppingCartRestClient {

    @RequestMapping("/cart/addProduct")
    String addProductToCart(@RequestParam("userid") String userId,
                            @RequestParam("asin") String asin);

    @RequestMapping("/cart/productsInCart")
    Map<String, Integer> getProductsInCart(@RequestParam("userid") String userId);

    @RequestMapping("/cart/removeProduct")
    String removeProductFromCart(@RequestParam("userid") String userId,
                                 @RequestParam("asin") String asin);
	
	@RequestMapping("/cart/clearCart")
    String clearCart(@RequestParam("userid") String userId);
}
