package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.cart.UpdateCartItemQtyDTO;
import com.metao.book.order.domain.ShoppingCart;
import com.metao.book.order.domain.dto.ShoppingCartDto;
import com.metao.book.order.presentation.dto.AddItemRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Validated
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingCartDto> getCart(@PathVariable String userId) {
        ShoppingCartDto cartDto = shoppingCartService.getCartForUser(userId);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/{userId}/{asin}")
    public ResponseEntity<ShoppingCart> addItemToCart(
            @PathVariable String userId,
            @PathVariable String asin,
            @RequestBody AddItemRequestDTO addItemRequestDTO) {
        ShoppingCart cartItem = shoppingCartService.addItemToCart(
                userId,
                asin,
                addItemRequestDTO.getQuantity(),
                addItemRequestDTO.getPrice(),
                addItemRequestDTO.getCurrency());
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/{userId}/{asin}")
    public ResponseEntity<ShoppingCart> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String asin,
            @RequestBody UpdateCartItemQtyDTO updateCartItemQtyDTO) {
        ShoppingCart cartItem = shoppingCartService.updateItemQuantity(
                userId,
                asin,
                updateCartItemQtyDTO.getQuantity());
        if (cartItem == null) {
            // This case handles when quantity is set to 0 or less, and item is removed.
            return ResponseEntity.noContent().build(); 
        }
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/{userId}/{asin}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String asin) {
        shoppingCartService.removeItemFromCart(userId, asin);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        shoppingCartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
