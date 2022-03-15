package com.metao.book.cart.controller;

import com.metao.book.cart.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping(value = "/add", produces = "application/json")
    public Map<String, Integer> addProductToCart(@RequestParam("userid") String userId,
            @RequestParam("asin") String asin) {
        shoppingCartService.addProductToShoppingCart(userId, asin);
        return shoppingCartService.getProductsInCart(userId);
    }

    @GetMapping(value = "/productsInCart", produces = "application/json")
    public Map<String, Integer> getProductsInCart(@RequestParam("userid") String userId) {
        return shoppingCartService.getProductsInCart(userId);
    }

    @DeleteMapping(value = "/removeProduct", produces = "application/json")
    public Map<String, Integer> removeProductFromCart(@RequestParam("userid") String userId,
            @RequestParam("asin") String asin) {
        shoppingCartService.removeProductFromCart(userId, asin);
        return shoppingCartService.getProductsInCart(userId);
    }

    @GetMapping(value = "/clearCart", produces = "application/json")
    public String clearCart(@RequestParam("userid") String userId) {
        shoppingCartService.clearCart(userId);
        return String.format("Clearing Cart, Checkout successful for user %s", userId);
    }

}