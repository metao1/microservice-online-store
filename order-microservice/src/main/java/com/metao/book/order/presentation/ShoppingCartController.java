package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartDto;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.cart.UpdateCartItemQtyDTO;
import com.metao.book.order.infrastructure.ShoppingCartMapper;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
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

    @PostMapping("/{userId}/{sku}")
    public ResponseEntity<ShoppingCartDto> addItemToCart(
            @PathVariable String userId,
            @PathVariable String sku,
        @RequestBody AddItemRequestDto dto
    ) {
        ShoppingCart cartItem = shoppingCartService.addItemToCart(
                userId,
                sku,
            dto.quantity(),
            dto.unitPrice(),
            dto.currency());
        var shoppingCardDto = ShoppingCartMapper.mapToDto(cartItem);
        return ResponseEntity.ok(shoppingCardDto);
    }

    @PutMapping("/{userId}/{sku}")
    public ResponseEntity<ShoppingCart> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String sku,
            @RequestBody UpdateCartItemQtyDTO updateCartItemQtyDTO) {
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
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String sku) {
        shoppingCartService.removeItemFromCart(userId, sku);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        shoppingCartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
