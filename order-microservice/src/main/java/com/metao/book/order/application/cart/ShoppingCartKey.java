package com.metao.book.order.application.cart;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"userId", "sku"})
public class ShoppingCartKey implements Serializable {

    private String userId;

    private String sku;
}
