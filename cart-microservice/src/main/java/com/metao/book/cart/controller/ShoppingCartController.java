package com.metao.book.cart.controller;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping(value = "/add", produces = "application/json")
    public String addProductToCart(@RequestParam("userid") String userId,
                                   @RequestParam("asin") String asin) {
        shoppingCartService.addProductToShoppingCart(userId, asin);
        return asin;
    }

    @GetMapping(value = "/productsInCart", produces = "application/json")
    public Map<String, List<ShoppingCartDto>> getProductsInCart(@RequestParam("userid") String userId) {
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

    @DeleteMapping(value = "/removeProduct", produces = "application/json")
    public String removeProductFromCart(@RequestParam("userid") String userId,
                                        @RequestParam("asin") String asin) {
        shoppingCartService.removeProductFromCart(userId, asin);
        return asin;
    }

    @GetMapping(value = "/clearCart", produces = "application/json")
    public String clearCart(@RequestParam("userid") String userId) {
        shoppingCartService.clearCart(userId);
        return String.format("Clearing Cart, Checkout successful for user %s", userId);
    }

}