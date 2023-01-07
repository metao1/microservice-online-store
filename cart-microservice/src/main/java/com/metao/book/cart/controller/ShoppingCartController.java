package com.metao.book.cart.controller;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.service.ShoppingCartService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/cart")
@RequiredArgsConstructor
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

    @PostMapping("/submit")
    public String submitProducts(
        @RequestParam("user_id") String userId
    ) {
        return shoppingCartService.submitProducts(userId);
    }

    @GetMapping(value = "/products")
    public Map<String, List<ShoppingCartDto>> getProductsInCart(@RequestParam("user_id") String userId) {
        return shoppingCartService.getProductsInCartByUserId(userId)
            .values()
            .stream()
            .flatMap(entry -> entry.stream().map(this::mapToShoppingCartDto))
            .collect(Collectors.groupingBy(ShoppingCartDto::getAsin));
    }

    private ShoppingCartDto mapToShoppingCartDto(ShoppingCart shoppingCart) {
        return ShoppingCartDto.builder()
            .asin(shoppingCart.getAsin())
            .quantity(shoppingCart.getQuantity())
            .build();
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
    public String clearCart(@RequestParam("user_id") String userId) {
        shoppingCartService.clearCart(userId);
        return String.format("Clearing Cart, Checkout successful for user %s", userId);
    }

}