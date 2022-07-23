package com.metao.book.cart.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"userId", "asin"})
public class ShoppingCartKey implements Serializable {

    private String userId;

    private String asin;
}
