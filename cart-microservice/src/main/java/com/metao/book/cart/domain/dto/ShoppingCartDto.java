package com.metao.book.cart.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ShoppingCartDto {

    private String asin;
    private int quantity;
}
