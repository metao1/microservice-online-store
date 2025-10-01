package com.metao.book.order.infrastructure;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartDto;
import com.metao.book.order.application.cart.ShoppingCartItem;
import java.util.Set;

public class ShoppingCartMapper {

    public static ShoppingCartDto mapToDto(ShoppingCart cartItem) {

        return new ShoppingCartDto(
            cartItem.getCreatedOn(),
            cartItem.getUserId(),
            Set.of(new ShoppingCartItem(
                cartItem.getSku(),
                cartItem.getQuantity(),
                cartItem.getBuyPrice(),
                cartItem.getCurrency())
            )
        );
    }
}
