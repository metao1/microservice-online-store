package com.metao.book.cart.controller;

import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.service.ShoppingCartService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping
    public String addProductToCart(
        @RequestParam("user_id") String userId,
        @RequestParam("asin") String asin
    ) {
        shoppingCartService.addProductToShoppingCart(userId, asin);
        return asin;
    }

    @GetMapping
    public Map<String, List<ShoppingCartDto>> getProductsInCart(@RequestParam("user_id") String userId) {
        return shoppingCartService.getProductsInCartByUserId(userId);
    }

    @PostMapping("/submit")
    public String submitProducts(
        @RequestParam("user_id") String userId
    ) {
        return shoppingCartService.submitProducts(userId);
    }

    @DeleteMapping(value = "/remove")
    public String removeProductFromCart(
        @RequestParam("user_id") String userId,
        @RequestParam("asin") String asin
    ) {
        shoppingCartService.removeProductFromCart(userId, asin);
        return asin;
    }

    @PutMapping(value = "/clear")
    public ResponseEntity<String> clearCart(@RequestParam("user_id") String userId) {
        var successSignal = shoppingCartService.clearCart(userId);
        if (successSignal > 0) {
            return ResponseEntity.ok(String.format("Clearing Cart, Checkout successful for user %s", userId));
        } else {
            return ResponseEntity.unprocessableEntity()
                .body(String.format("Clearing Cart, checkout unsuccessful for user %s", userId));
        }
    }

}