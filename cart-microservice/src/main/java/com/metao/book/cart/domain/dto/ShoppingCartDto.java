package com.metao.book.cart.domain.dto;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShoppingCartDto {

    String userId;
    Long createdOn;
    Set<ShoppingCartItem> shoppingCartItems = new LinkedHashSet<>();

    public ShoppingCartDto(String userId, Long createdOn) {
        this.userId = userId;
        this.createdOn = createdOn;
    }

    public void addItem(ShoppingCartItem shoppingCartItem) {
        this.shoppingCartItems.add(shoppingCartItem);
    }
}
