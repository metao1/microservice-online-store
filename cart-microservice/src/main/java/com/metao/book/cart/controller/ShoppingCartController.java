package com.metao.book.cart.controller;

import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.service.ShoppingCartService;
import com.metao.book.cart.service.mapper.CartMapperService;
import com.metao.book.cart.service.mapper.ShoppingCartMapper;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final CartMapperService.ToEventMapper cartMapper;
    private final ShoppingCartMapper shoppingCartMapper;

    @PostMapping
    public String addProductToCart(
        @RequestParam("user_id") String userId,
        @RequestParam("asin") String asin
    ) {
        shoppingCartService.addOrderToShoppingCart(userId, asin);
        return asin;
    }

    @GetMapping
    public ShoppingCartDto getProductsInCart(@RequestParam("user_id") String userId) {
        final var productsInCartByUserId = shoppingCartService.getProductsInCartByUserId(userId);
        final var shoppingCartItems = productsInCartByUserId
            .stream()
            .map(cartMapper::mapToOrderEvent)
            .map(item -> shoppingCartMapper.mapToEvent(() -> item, Objects::nonNull)
            ).collect(Collectors.toSet());
        return new ShoppingCartDto(Instant.now().toEpochMilli(), shoppingCartItems);
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitProducts(
        @RequestParam("user_id") String userId
    ) {
        final var submitProductsOptional = shoppingCartService.submitProducts(userId);
        if (submitProductsOptional.isPresent()) {
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(String.format("Successfully submitted for user %s", userId));
        } else {
            return ResponseEntity.unprocessableEntity()
                .body(String.format("Submitting products unsuccessful for user %s", userId));
        }
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