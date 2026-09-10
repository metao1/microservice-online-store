package com.metao.book.order.presentation;

import com.metao.book.order.application.usecase.ShoppingCartUseCase;
import com.metao.book.order.presentation.dto.ShoppingCartResponseDto;
import com.metao.book.order.presentation.dto.ShoppingCartItemDto;
import com.metao.book.order.presentation.dto.UpdateCartItemQtyDto;
import com.metao.book.shared.architecture.InboundAdapter;
import com.metao.book.shared.security.CurrentUser;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_cart:read')")
    public ShoppingCartResponseDto getCart() {
        String userId = CurrentUser.subject();
        return ShoppingCartResponseDto.from(shoppingCartUseCase.getCartForUser(userId));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_cart:write')")
    public int addItemToCart(
        @Valid @RequestBody List<ShoppingCartItemDto> items
    ) {
        String userId = CurrentUser.subject();
        return shoppingCartUseCase.addItemToCart(userId, items.stream()
            .map(ShoppingCartItemDto::toApplicationItem)
            .toList());
    }

    @PutMapping("/items/{sku}")
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_cart:write')")
    public ResponseEntity<ShoppingCartResponseDto> updateItemQuantity(
        @PathVariable String sku,
        @Valid @RequestBody UpdateCartItemQtyDto updateCartItemQtyDto
    ) {
        String userId = CurrentUser.subject();
        var cartItem = shoppingCartUseCase.updateItemQuantity(userId,
            sku,
            updateCartItemQtyDto.quantity());
        if (cartItem == null) {
            // This case handles when quantity is set to 0 or less, and item is removed.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ShoppingCartResponseDto.from(cartItem));
    }

    @DeleteMapping("/items/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_cart:write')")
    public void removeItemFromCart(
        @PathVariable String sku
    ) {
        String userId = CurrentUser.subject();
        shoppingCartUseCase.removeItemFromCart(userId, sku);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_cart:write')")
    public void clearCart() {
        String userId = CurrentUser.subject();
        shoppingCartUseCase.clearCart(userId);
    }
}
