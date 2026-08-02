package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCartUseCase;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.ShoppingCartResponseDto;
import com.metao.book.order.presentation.dto.UpdateCartItemQtyDto;
import com.metao.book.shared.architecture.InboundAdapter;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@InboundAdapter(InboundAdapter.Kind.HTTP)
@RequestMapping("/cart")
@RequiredArgsConstructor
@Validated
@Timed(value = "order.cart.api", extraTags = {"controller", "shopping-cart"})
@Observed(name = "order.cart.api.controller", contextualName = "shopping-cart-controller")
public class ShoppingCartController {

    private final ShoppingCartUseCase shoppingCartUseCase;

    @GetMapping("/{userId}")
    public ShoppingCartResponseDto getCart(@PathVariable String userId) {
        return ShoppingCartResponseDto.from(shoppingCartUseCase.getCartForUser(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public int addItemToCart(
        @Valid @RequestBody AddItemRequestDto dto
    ) {
        return shoppingCartUseCase.addItemToCart(dto.userId(), dto.items());
    }

    @PutMapping("/{userId}/{sku}")
    public ResponseEntity<ShoppingCartResponseDto> updateItemQuantity(
        @PathVariable String userId,
        @PathVariable String sku,
        @Valid @RequestBody UpdateCartItemQtyDto updateCartItemQtyDto
    ) {
        var cartItem = shoppingCartUseCase.updateItemQuantity(userId,
            sku,
            updateCartItemQtyDto.quantity());
        if (cartItem == null) {
            // This case handles when quantity is set to 0 or less, and item is removed.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ShoppingCartResponseDto.from(cartItem));
    }

    @DeleteMapping("/{userId}/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItemFromCart(
        @PathVariable String userId,
        @PathVariable String sku
    ) {
        shoppingCartUseCase.removeItemFromCart(userId, sku);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable String userId) {
        shoppingCartUseCase.clearCart(userId);
    }
}
