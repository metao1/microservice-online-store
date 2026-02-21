package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartDto;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.cart.UpdateCartItemQtyDTO;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import lombok.RequiredArgsConstructor;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Validated
@Timed(value = "order.cart.api", extraTags = {"controller", "shopping-cart"})
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping("/{userId}")
    public ShoppingCartDto getCart(@PathVariable String userId) {
        return shoppingCartService.getCartForUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public int addItemToCart(
        @Validated @RequestBody AddItemRequestDto dto
    ) {
        return shoppingCartService.addItemToCart(
            dto.userId(), dto.items()
        );
    }

    @PutMapping("/{userId}/{sku}")
    public ResponseEntity<ShoppingCart> updateItemQuantity(
        @PathVariable String userId,
        @PathVariable String sku,
        @RequestBody UpdateCartItemQtyDTO updateCartItemQtyDTO
    ) {
        ShoppingCart cartItem = shoppingCartService.updateItemQuantity(userId,
            sku,
            updateCartItemQtyDTO.quantity());
        if (cartItem == null) {
            // This case handles when quantity is set to 0 or less, and item is removed.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/{userId}/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItemFromCart(
        @PathVariable String userId,
        @PathVariable String sku
    ) {
        shoppingCartService.removeItemFromCart(userId, sku);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable String userId) {
        shoppingCartService.clearCart(userId);
    }
}
