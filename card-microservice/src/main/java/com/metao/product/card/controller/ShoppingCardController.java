package com.metao.product.card.controller;

import com.metao.product.card.service.ShoppingCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/cart")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShoppingCardController {

    private final ShoppingCardService shoppingCardService;

    @GetMapping(value = "/shoppingCard/addProduct", produces = "application/json")
    public String addProductToCart(@RequestParam("userid") String userId,
                                   @RequestParam("asin") String asin) {
        shoppingCardService.addProductToShoppingCard(userId, asin);
        return String.format("Added to Card successful for user %s", userId);
    }

    @GetMapping(value = "/shoppingCard/productsInCart", produces = "application/json")
    public Map<String, Integer> getProductsInCart(@RequestParam("userid") String userId) {
        return shoppingCardService.getProductsInCard(userId);
    }

    @GetMapping(value = "/shoppingCard/removeProduct", produces = "application/json")
    public String removeProductFromCart(@RequestParam("userid") String userId,
                                        @RequestParam("asin") String asin) {
        shoppingCardService.removeProductFromCard(userId, asin);
        return String.format("Removing from Card successful for user %s", userId);
    }

    @GetMapping(value = "/shoppingCard/clearCart", produces = "application/json")
    public String clearCart(@RequestParam("userid") String userId) {
        shoppingCardService.clearCard(userId);
        return String.format("Clearing Card, Checkout successful for user %s", userId);
    }

}